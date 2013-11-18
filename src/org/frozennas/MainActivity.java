package org.frozennas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;

import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import ar.com.daidalos.afiledialog.FileChooserDialog;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.ClientConfiguration;
import com.amazonaws.Protocol;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.GetObjectMetadataRequest;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.RestoreObjectRequest;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.services.s3.model.StorageClass;
import com.amazonaws.services.s3.transfer.Download;
import com.amazonaws.services.s3.transfer.TransferManager;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.Security;

import az.jefsr.Volume;
import az.jefsr.crypto.CipherDataException;

public class MainActivity extends Activity implements RestoreObjectDialog.RestoreDialogListener,
													 	DownloadObjectDialog.DownloadDialogListener {
	
	private FileListAdapter adpt;
	private String currentfolder = "";
	private Button backbutton;
	private Button foldername_button;
	private Volume encfsvolume;
	private MyS3ObjectSummary objectToProcess;
    private String downloadDir;
    private boolean preferences_updated;
    ListView lView;

    //preferences
    private String s3accesskey_preference; 
    private String s3seckey_preference;
    private String s3bucket_preference;
    private String s3rootfolder_preference;
    private String encfskey_preference;
    private String availabilitydays_preference;
    private String encfsvolumefile_preference;
    
	static {
	    Security.insertProviderAt(new org.spongycastle.jce.provider.BouncyCastleProvider(), 1);
	}
	
	private AmazonS3Client s3Client; 
	private TransferManager tx;
	
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        s3accesskey_preference = sharedPref.getString("s3accesskey_preference", ""); 
        s3seckey_preference = sharedPref.getString("s3seckey_preference", "");
        s3bucket_preference = sharedPref.getString("s3bucket_preference", "");
        s3rootfolder_preference = sharedPref.getString("s3rootfolder_preference", "");
        encfskey_preference = sharedPref.getString("encfskey_preference", "");
        availabilitydays_preference = sharedPref.getString("availabilitydays_preference", "");
        encfsvolumefile_preference = sharedPref.getString("encfsvolumefile_preference", "");
   
        preferences_updated = true;
        
        foldername_button = (Button) findViewById(R.id.foldername_button);
        foldername_button.setClickable(false);
        
        backbutton = (Button) findViewById(R.id.button1);
        addListenerOnButton();
        
        //FileListAdapter needs dummy element for creation
        ArrayList<MyS3ObjectSummary> dummylist = new ArrayList<MyS3ObjectSummary>();
        dummylist.add(new MyS3ObjectSummary("dummy"));
        adpt  = new FileListAdapter(dummylist, this);
        lView = (ListView) findViewById(R.id.listview);
        lView.setVisibility(View.GONE);
        lView.setAdapter(adpt);

        lView.setOnItemLongClickListener(new OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int pos, long id) {
				MyS3ObjectSummary c = adpt.getItem(pos);
				
				if (c.isFolder()) {
					//folder long clicked
				}
				else {
					//file long clicked
					objectToProcess = c;
					
					Toast.makeText(getApplicationContext(),"Encrypted name: " + c.getKey(), Toast.LENGTH_LONG).show();
				}
				return true;
            }
        });
        
        lView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int pos,
					long id) {
				 
				MyS3ObjectSummary c = adpt.getItem(pos);
				
				if (c.isFolder()) {
					//folder clicked
					currentfolder = c.getKey();					
					adpt.clear();
					foldername_button.setText("/" + c.getDecryptedKey());
					(new AsyncListViewLoader()).execute(currentfolder);					
				}
				else {
					//file clicked
					objectToProcess = c;

					if(!c.getStorageClass().equals(StorageClass.Glacier.toString())){						
							//non-glacier file
							DownloadObjectDialog downloaddialog = new DownloadObjectDialog();
							downloaddialog.show(getFragmentManager(), "download");
						}
						else{
							//TODO
							//check if glacier file is already restored
							//if so, offer download
							//if not, offer restore
				            
							(new AsyncObjectRestoreStatus()).execute(objectToProcess.getKey());
						}
				}		
			}
        });
        
    }   
        
    
    @Override
    protected void onStart() {
        super.onStart(); 

        backbutton.setClickable(false);
        //lView.setClickable(false);
        
        if(preferences_updated && checkPreferences()) {
            //reinitiate after preference change
        	Log.v(Constants.TAG, "reinitiate after preference change");

        	encfsvolume = null;

        	//workaround for buckets containing dots. WTF!!
        	ClientConfiguration clientConfig = new ClientConfiguration();
        	clientConfig.setProtocol(Protocol.HTTP);
        	
        	try{
        	s3Client = new AmazonS3Client(
        			new BasicAWSCredentials(s3accesskey_preference, s3seckey_preference), clientConfig
        			);

        	//s3Client.setRegion(Region.getRegion(Regions.EU_WEST_1));        	
        	//s3Client.setEndpoint("s3-eu-west-1.amazonaws.com");
        	
        	tx = new TransferManager(s3Client);
        	} catch (Exception e) {
        		Log.v(Constants.TAG, "Exception: " + e.getMessage());
        	}
        	currentfolder = s3rootfolder_preference;
        	
        	backbutton.setClickable(true);
        	//lView.setClickable(true);
        	
        	preferences_updated = false;
        	
        	// Exec async load task
        	(new AsyncListViewLoader()).execute(currentfolder);
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart(); 
    
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String new_s3accesskey_preference = sharedPref.getString("s3accesskey_preference", ""); 
        String new_s3seckey_preference = sharedPref.getString("s3seckey_preference", "");
        String new_s3bucket_preference = sharedPref.getString("s3bucket_preference", "");
        String new_s3rootfolder_preference = sharedPref.getString("s3rootfolder_preference", "");
        String new_encfskey_preference = sharedPref.getString("encfskey_preference", "");
        String new_availabilitydays_preference = sharedPref.getString("availabilitydays_preference", "");
        String new_encfsvolumefile_preference = sharedPref.getString("encfsvolumefile_preference", "");
                
		if (!s3accesskey_preference.equals(new_s3accesskey_preference)) {
			s3accesskey_preference = new_s3accesskey_preference;
			preferences_updated = true;
		}
		if (!s3seckey_preference.equals(new_s3seckey_preference)) {
			s3seckey_preference = new_s3seckey_preference;
			preferences_updated = true;
		}
		if (!s3bucket_preference.equals(new_s3bucket_preference)) {
			s3bucket_preference = new_s3bucket_preference;
			preferences_updated = true;
		}
		if (!s3rootfolder_preference.equals(new_s3rootfolder_preference)) {
			s3rootfolder_preference = new_s3rootfolder_preference;
			preferences_updated = true;
		}
		if (!encfskey_preference.equals(new_encfskey_preference)) {
			encfskey_preference = new_encfskey_preference;
			preferences_updated = true;
		}
		if (!availabilitydays_preference.equals(new_availabilitydays_preference)) {
			availabilitydays_preference = new_availabilitydays_preference;
			preferences_updated = true;
		}
		if (!encfsvolumefile_preference.equals(new_encfsvolumefile_preference)) {
			encfsvolumefile_preference = new_encfsvolumefile_preference;
			preferences_updated = true;
		}
    }    
    
	private boolean checkPreferences() {
		
		boolean check_result = false;
		
		//TODO implement some real checks
		if (
	        !s3accesskey_preference.equals("") && 
	        !s3seckey_preference.equals("") &&
	        !s3bucket_preference.equals("") &&
	        !s3rootfolder_preference.equals("") &&
	        !encfskey_preference.equals("") &&
	        !availabilitydays_preference.equals("") &&
	        !encfsvolumefile_preference.equals("")
		) {
			check_result = true;
		}
		
		return check_result;
	}

	private void addListenerOnButton() {
		 
		backbutton.setOnClickListener(new OnClickListener() {
 
			public void onClick(View arg0) {
				backbutton.setClickable(false);
				if(!currentfolder.equals(s3rootfolder_preference))
				{
					String decrypted_foldername = foldername_button.getText().toString();
					decrypted_foldername = decrypted_foldername.substring(0, decrypted_foldername.lastIndexOf("/"));
					//Back to root folder -> "/"
					if (decrypted_foldername.length() == 0){
						foldername_button.setText("/");
					}
					else {
						foldername_button.setText(decrypted_foldername);
					}	
					
					int end = currentfolder.substring(0, currentfolder.length() - 1).lastIndexOf("/") + 1;
					currentfolder = currentfolder.substring(0, end);
					adpt.clear();
					(new AsyncListViewLoader()).execute(currentfolder);					
				}
				backbutton.setClickable(true);
			}
		});		
	}

    private class AsyncListViewLoader extends AsyncTask<String, Void, List<MyS3ObjectSummary>> {
    	private final ProgressDialog dialog = new ProgressDialog(MainActivity.this);
    	
		@Override
		protected void onPostExecute(List<MyS3ObjectSummary> result) {			
			super.onPostExecute(result);
			
			dialog.dismiss();
			adpt.setItemList(result);			
			adpt.notifyDataSetChanged();
			lView.setVisibility(View.VISIBLE);
		}

		@Override
		protected void onPreExecute() {		
			super.onPreExecute();
						
			dialog.setMessage("Downloading object list of bucket " + s3bucket_preference);
			dialog.show();
			
		}
		

		@Override
		protected List<MyS3ObjectSummary> doInBackground(String... params) {
			List<S3ObjectSummary> s3object_list = new ArrayList<S3ObjectSummary>();
			List<String> s3common_prefixes_list = new ArrayList<String>();
			List<MyS3ObjectSummary> mys3object_list = new ArrayList<MyS3ObjectSummary>();
						
			try {
			
				//init decryption asyncronously
				if (encfsvolume == null) {
					encfsvolume = new az.jefsr.Volume(".encfs6.xml", new FileInputStream(encfsvolumefile_preference));
					encfsvolume.init(encfskey_preference);
				}
				
				//get all files in root folder
				ListObjectsRequest objectRequest = new ListObjectsRequest();
				objectRequest = objectRequest.withBucketName(s3bucket_preference);
				objectRequest = objectRequest.withPrefix(params[0]);
				objectRequest = objectRequest.withMaxKeys(500);
				objectRequest = objectRequest.withDelimiter("/");

				ObjectListing listOfObjects = s3Client.listObjects(objectRequest);
				s3object_list = listOfObjects.getObjectSummaries(); //get files initially
				
				do 
				{
					
					//handle files
					for(S3ObjectSummary os: s3object_list ) {
						MyS3ObjectSummary mos = new MyS3ObjectSummary(os);
	
						//try to decrypt name
						String decryptedkey = "";
						try{
							
							decryptedkey = encfsvolume.decryptPath(mos.getKey().substring(s3rootfolder_preference.length()));
							
							//do not add current folder to list
							if(!mos.getKey().equals(params[0])) {
								mos.setDecryptedKey(decryptedkey);
								mys3object_list.add(mos);	
							}							

						} catch (CipherDataException e) {
							//decryption error or name not encrypted -> sure original object key name
							Log.v(Constants.TAG, "Object not encrypted: " + mos.getKey());
						}			
						
						
					}	
					
					//handle folders
					s3common_prefixes_list = listOfObjects.getCommonPrefixes(); 
					for(String cp: s3common_prefixes_list) {
						MyS3ObjectSummary mos = new MyS3ObjectSummary(cp);
						
						//try to decrypt name
						String decryptedkey = "";
						try{
							
							decryptedkey = encfsvolume.decryptPath(mos.getKey().substring(s3rootfolder_preference.length()));

							mos.setDecryptedKey(decryptedkey);
							mys3object_list.add(mos);			
						} catch (CipherDataException e) {
							//decryption error or name not encrypted -> log original object key name
							Log.v(Constants.TAG, "Object not encrypted: " + mos.getKey());
						}			
					}

					listOfObjects = s3Client.listNextBatchOfObjects(listOfObjects);
					s3object_list.clear();
					s3object_list.addAll(listOfObjects.getObjectSummaries()); //files
					
				} while (listOfObjects.isTruncated());
				
				//sort mys3object_list
				Comparator<MyS3ObjectSummary> MyS3ObjectSummaryComparator = new Comparator<MyS3ObjectSummary>() {
				    public int compare(MyS3ObjectSummary o1, MyS3ObjectSummary o2) {
				        return o1.getDecryptedKey().compareToIgnoreCase(o2.getDecryptedKey()); 
				    }
				};				
				
				Collections.sort(mys3object_list,MyS3ObjectSummaryComparator);
				
				return mys3object_list;
			}
			catch(Exception e) {
				Log.v(Constants.TAG, "Exception: " + e.getMessage());
				Toast.makeText(getApplicationContext(),"Exception: " + e.getMessage(), Toast.LENGTH_SHORT).show();
			}
			return null;
		}
    }

    private class AsyncObjectRestoreStatus extends AsyncTask<String, Void, String> {
    	private final ProgressDialog dialog = new ProgressDialog(MainActivity.this);

				
		@Override
		protected void onPreExecute() {		
			super.onPreExecute();
						
			dialog.setMessage("Downloading meta data of object...");
			dialog.show();
		}

		@Override
		protected void onPostExecute(String s) {		
			super.onPostExecute(s);
			
			dialog.dismiss();
			
			if(s == "RUNNINGRESTORE"){
				runOnUiThread(new Runnable() {
				@Override public void run() { 
					Toast.makeText(getApplicationContext(),"Restore request successful. Restoration status: in progress", Toast.LENGTH_LONG).show();
				} });
			}
			if(s == "COMPLETEDRESTORE"){
				//restore completed, start download dialog
				DownloadObjectDialog downloaddialog = new DownloadObjectDialog();
				downloaddialog.show(getFragmentManager(), "download");
				
			}
			if(s == "STILLINGLACIER"){
            	//file is still locked in the glacier, ask user to restore it
				RestoreObjectDialog restoredialog = new RestoreObjectDialog();
				restoredialog.show(getFragmentManager(), "restore");				            	
				
			}
		}		
		
		@Override
		protected String doInBackground(String... params) {
			String result = "";
			
			GetObjectMetadataRequest requestCheck = new GetObjectMetadataRequest(s3bucket_preference, params[0]);
            ObjectMetadata response = s3Client.getObjectMetadata(requestCheck);
            try{
            if(response.getOngoingRestore())
            {
            	result = "RUNNINGRESTORE";
            }else {            	
                result = "COMPLETEDRESTORE";
            }
            } catch (Exception e) {
            	//Log.v(Constants.TAG, "getOngoingRestore: NULL" );
            	//Log.v(Constants.TAG, "Exception: " + e.getMessage());
            	result = "STILLINGLACIER";
            }

			return result;
			
		}
    }
    
    private class AsyncRestoreRequest extends AsyncTask<String, Void, Boolean> {

    	protected void onPostExecute(Boolean b) {			
			super.onPostExecute(b);
			if (b == true)
				runOnUiThread(new Runnable() {
				@Override public void run() { 
					Toast.makeText(getApplicationContext(),"Restore request successful. Restoration status: in progress", Toast.LENGTH_SHORT).show();
				} });
    	}			
		
		@Override
		protected Boolean doInBackground(String... params) {
			Boolean restoreFlag = false;
			
	        try {
	            RestoreObjectRequest requestRestore = new RestoreObjectRequest(s3bucket_preference, params[0], Integer.parseInt(availabilitydays_preference));
	            s3Client.restoreObject(requestRestore);
	            
	            GetObjectMetadataRequest requestCheck = new GetObjectMetadataRequest(s3bucket_preference, params[0]);
	            ObjectMetadata response = s3Client.getObjectMetadata(requestCheck);
	            
	            restoreFlag = response.getOngoingRestore();

	            return restoreFlag;
	          } catch (AmazonS3Exception amazonS3Exception) {
	        	  Log.v(Constants.TAG, amazonS3Exception.toString());
	          } catch (Exception e) {
	        	  Log.v(Constants.TAG, e.toString());
	          }     		
			
			return null;
		}

		@Override
		protected void onPreExecute() {		
			super.onPreExecute();
		}
		
    }
    
    private class AsyncDownloadRequest extends AsyncTask<String, Void, Void> {
    	
    	private File downloadFile;
    	
		@Override
    	protected void onPostExecute(Void v) {			
			super.onPostExecute(v);
			runOnUiThread(new Runnable() {
				@Override public void run() { 
					Toast.makeText(getApplicationContext(),"Download of " + downloadFile.getName() + " completed", Toast.LENGTH_SHORT).show();					
				} });			
		}


		@Override
		protected Void doInBackground(String... params) {

			String downloadTarget = new String(params[0] + "/" + objectToProcess.getDecryptedKey().substring(objectToProcess.getDecryptedKey().lastIndexOf("/")+1) + "enc");
			Log.v(Constants.TAG, "Download Target: " + downloadTarget);
			
			downloadFile = new File(downloadTarget);

			try {

				if(!downloadFile.exists()) {
					downloadFile.createNewFile();
				}
	        	
	        	Download myDownload = tx.download(s3bucket_preference, params[1], downloadFile);

	        	 myDownload.waitForCompletion();	
      			 Log.v(Constants.TAG, "Download completed");
      			 
	        	 BufferedInputStream bufStream = new BufferedInputStream(new FileInputStream(downloadFile));
	        	 
	        	 File decryptfile = new File(downloadTarget.substring(0, downloadTarget.length() - 3));
				 if(!decryptfile.exists()) {
					 decryptfile.createNewFile();
			   	 }	         	 
	        	 FileOutputStream fos = new FileOutputStream (decryptfile);
	        	 ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
	        	 
	        	 encfsvolume.decryptFile(objectToProcess.getKey().substring(s3rootfolder_preference.length()), bufStream, byteOut);

	        	 byteOut.writeTo(fos);
	        	 
	        	 
	        	 downloadFile.delete();
	        	 Log.v(Constants.TAG, "Decryption completed");
	        	 
	         } catch (AmazonServiceException e) {
	        	  Log.v(Constants.TAG, e.toString());
	  			runOnUiThread(new Runnable() {
					@Override public void run() { 	        	  
	        	  Toast.makeText(getApplicationContext(),"Service Exception!", Toast.LENGTH_SHORT).show();
				} });
	        	  
	         } catch (AmazonClientException e) {
	        	  Log.v(Constants.TAG, e.toString());
	        	  runOnUiThread(new Runnable() {
				@Override public void run() { 	        	  
	        	  Toast.makeText(getApplicationContext(),"Client Exception!", Toast.LENGTH_SHORT).show();
				} });
	        	  
	         } catch (Exception e) {
	        	  Log.v(Constants.TAG, e.toString());
	        	  runOnUiThread(new Runnable() {
				@Override public void run() { 	        	  
	        	  Toast.makeText(getApplicationContext(),"Exception!", Toast.LENGTH_SHORT).show();
				} });
	        	  
	         }     		
			
			return null;
		}

		@Override
		protected void onPreExecute() {		
			super.onPreExecute();
			
		}
		
    }


	@Override
	public void onRestoreDialogPositiveClick(DialogFragment dialog) {

        (new AsyncRestoreRequest()).execute(objectToProcess.getKey());    
        
	}

	@Override
	public void onRestoreDialogNegativeClick(DialogFragment dialog) {
		
	}

	@Override
	public void onDownloadDialogPositiveClick(DialogFragment dialog) {
		
        // Create DirectoryChooserDialog and register a callback
		FileChooserDialog directoryChooserDialog = new FileChooserDialog(MainActivity.this);
		directoryChooserDialog.setCanCreateFiles(true);
		directoryChooserDialog.setFolderMode(true);
		directoryChooserDialog.addListener(new FileChooserDialog.OnFileSelectedListener() {
			@Override
			public void onFileSelected(Dialog source, File file) {
				downloadDir = file.getAbsolutePath();
				source.hide();
				
                Toast.makeText(
                		MainActivity.this, "Chosen directory: " + 
                		file.getName(), Toast.LENGTH_SHORT).show();
                
                (new AsyncDownloadRequest()).execute(downloadDir, objectToProcess.getKey());
	         }
			@Override
	         public void onFileSelected(Dialog source, File folder, String name) {
				downloadDir = folder.getAbsolutePath();
				source.hide();
				
                Toast.makeText(
                		MainActivity.this, "Chosen directory: " + 
                		folder.getAbsolutePath(), Toast.LENGTH_SHORT).show();
                
                (new AsyncDownloadRequest()).execute(downloadDir, objectToProcess.getKey());	    
             }
		});						
		
		directoryChooserDialog.show();						
		
        /*DirectoryChooserDialog directoryChooserDialog = 
        new DirectoryChooserDialog(MainActivity.this, 
            new DirectoryChooserDialog.ChosenDirectoryListener() 
        {
            @Override
            public void onChosenDir(String chosenDir) 
            {
            	downloadDir = chosenDir;
                Toast.makeText(
                		MainActivity.this, "Chosen directory: " + 
                		downloadDir, Toast.LENGTH_SHORT).show();
                
                (new AsyncDownloadRequest()).execute(downloadDir, objectToProcess.getKey());
            }
        }); 
        // Toggle new folder button enabling
        directoryChooserDialog.setNewFolderEnabled(true);
        // Load directory chooser dialog for initial 'm_chosenDir' directory.
        // The registered callback will be called upon final directory selection.
        directoryChooserDialog.chooseDirectory();*/

	}

	@Override
	public void onDownloadDialogNegativeClick(DialogFragment dialog) {
		
	}
	
	//ACTION BAR and SETTINGS ACTIVITY
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    // Inflate the menu items for use in the action bar
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.main_activity_actions, menu);
	    return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle presses on the action bar items
	    switch (item.getItemId()) {
	        case R.id.action_settings:

	        	startActivity(new Intent(this, SettingsActivity.class));
	        	
	            return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}
	
}

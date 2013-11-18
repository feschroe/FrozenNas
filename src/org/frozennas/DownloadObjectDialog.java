package org.frozennas;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

public class DownloadObjectDialog extends DialogFragment {

	DownloadDialogListener DownListener;
	
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("Download this object to your device?")
               .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {

                	   DownListener.onDownloadDialogPositiveClick(DownloadObjectDialog.this);

                   }
               })
               .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {

                	   DownListener.onDownloadDialogNegativeClick(DownloadObjectDialog.this);                	   
                	   
                   }
               });
        return builder.create();
    }
	
	public interface DownloadDialogListener {
	        public void onDownloadDialogPositiveClick(DialogFragment dialog);
	        public void onDownloadDialogNegativeClick(DialogFragment dialog);
	    }	    

	    @Override
	    public void onAttach(Activity activity) {
	        super.onAttach(activity);
	        try {
	        	DownListener = (DownloadDialogListener) activity;
	        } catch (ClassCastException e) {
	            throw new ClassCastException(activity.toString()
	                    + " must implement NoticeDialogListener");
	        }
	    }
	    
	    @Override
	    public void onDetach() {
	    	super.onDetach();
	    	DownListener = null;
	    }
	
}
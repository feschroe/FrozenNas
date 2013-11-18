package org.frozennas;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

public class RestoreObjectDialog extends DialogFragment {

	RestoreDialogListener RestoreListener;
	
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("Restore this object from Glacier?")
               .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                	   
                	   RestoreListener.onRestoreDialogPositiveClick(RestoreObjectDialog.this);

                   }
               })
               .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {

                	   RestoreListener.onRestoreDialogNegativeClick(RestoreObjectDialog.this);                	   
                	   
                   }
               });
        return builder.create();
    }
	
	public interface RestoreDialogListener {
	        public void onRestoreDialogPositiveClick(DialogFragment dialog);
	        public void onRestoreDialogNegativeClick(DialogFragment dialog);
	    }	    

	    
	    @Override
	    public void onAttach(Activity activity) {
	        super.onAttach(activity);

	        try {
	        
	        	RestoreListener = (RestoreDialogListener) activity;

	        } catch (ClassCastException e) {
	            throw new ClassCastException(activity.toString()
	                    + " must implement NoticeDialogListener");
	        }
	    }
	    
	    @Override
	    public void onDetach() {
	    	super.onDetach();
	    	RestoreListener = null;
	    }
	
}
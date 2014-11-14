package com.modusgo.ubi;

import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import com.modusgo.ubi.requesttasks.BasePostRequestAsyncTask;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.widget.EditText;
import android.widget.Toast;

public class DialogFeedback extends DialogFragment {
	
	String currentScreen;
	String comment;
	
	public DialogFeedback(String currentScreen) {
		this.currentScreen = currentScreen;
	}
	
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		Context ctx = getActivity();
	    ctx.setTheme(android.R.style.Theme_Holo_Light);
	    
		AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
	    // Get the layout inflater
	    LayoutInflater inflater = getActivity().getLayoutInflater();

	    // Inflate and set the layout for the dialog
	    // Pass null as the parent view because its going in the dialog layout
	    builder.setView(inflater.inflate(R.layout.dialog_feedback, null))
	    // Add action buttons
	    .setTitle("Feedback on "+currentScreen)
	    .setPositiveButton("Send", new DialogInterface.OnClickListener() {
	    	@Override
	        public void onClick(DialogInterface dialog, int id) {
	    		Dialog f = (Dialog) dialog;
	    		final EditText editComment = (EditText) f.findViewById(R.id.editComment);
                
	    		comment = editComment.getText().toString();
	    		new SendFeedback(getActivity()).execute("feedback");
	        }
	    })
	    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
	    	public void onClick(DialogInterface dialog, int id) {
	    		dialog.cancel();
	        }
	    });
		return builder.create();
	}
	
	class SendFeedback extends BasePostRequestAsyncTask{

		public SendFeedback(Context context) {
			super(context);
			requestParams.add(new BasicNameValuePair("page", currentScreen));
			requestParams.add(new BasicNameValuePair("comments", comment));
		}
		
		@Override
		protected void onSuccess(JSONObject responseJSON) throws JSONException {
			if(responseJSON.has("status") && responseJSON.getString("status").equals("success")){
				Toast.makeText(context, "Thank you for your feedback!\nWe will review as soon as possible.", Toast.LENGTH_SHORT).show();
			}
			else{
				if(responseJSON.has("error"))
					Toast.makeText(context, responseJSON.getString("error"), Toast.LENGTH_SHORT).show();
				else
					Toast.makeText(context, "Something gone wrong", Toast.LENGTH_SHORT).show();
			}
			super.onSuccess(responseJSON);
		}
	}
}

package org.frozennas;

import java.util.List;

import org.apache.commons.io.FileUtils;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;


public class FileListAdapter extends ArrayAdapter<MyS3ObjectSummary> {
	
	private List<MyS3ObjectSummary> itemList;
	private Context context;
		
	public FileListAdapter(List<MyS3ObjectSummary> itemList, Context ctx) {
		super(ctx, R.layout.list_item, itemList);
		this.itemList = itemList;
		this.context = ctx;		
	}

	@Override
	public int getCount() {
		if (itemList != null)
			return itemList.size();
		return 0;
	}
	
	@Override
	public MyS3ObjectSummary getItem(int position) {
		if (itemList != null)
			return itemList.get(position);
		return null;
	}
	
/*
	public long getItemId(int position) {
		if (itemList != null)
			return itemList.get(position).hashCode();
		return 0;
	}
*/	

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		View v = convertView;
		if (v == null) {
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = inflater.inflate(R.layout.list_item, null);
		}
		
		MyS3ObjectSummary obj = itemList.get(position);
		String decryptedname = obj.getDecryptedKey();
		
		//image
		ImageView imageView = (ImageView) v.findViewById(R.id.logo);
		imageView.setClickable(false);
		

		if (obj.isFolder()) {
			imageView.setImageResource(R.drawable.folder);
		}
		else {
			imageView.setImageResource(R.drawable.files);
		}	
		
		//name
		TextView text = (TextView) v.findViewById(R.id.name);
		//root folder
		text.setText(decryptedname);
		if(decryptedname.lastIndexOf("/") == -1){
			text.setText(decryptedname);
		} else {
			text.setText(decryptedname.substring(decryptedname.lastIndexOf("/")+1));
		}
			
		//storageclass	
		TextView text1 = (TextView) v.findViewById(R.id.storageclass);
		if (obj.isFolder()) {
			text1.setText("");
		}
		else {
			text1.setText(obj.getStorageClass());
		}		
		
		//size
		TextView text2 = (TextView) v.findViewById(R.id.sizeofobject);
		if (obj.isFolder()) {
			text2.setText("");
		}
		else {
			text2.setText(FileUtils.byteCountToDisplaySize(obj.getSize()));
		}	

		return v;		
	}

	public List<MyS3ObjectSummary> getItemList() {
		return itemList;
	}

	public void setItemList(List<MyS3ObjectSummary> itemList) {
		this.itemList = itemList;
	}	
}

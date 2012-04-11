package com.hunterdavis.sobersecrets;

import java.util.Random;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.ads.AdRequest;
import com.google.ads.AdView;

public class SoberSecrets extends Activity {

	// setup our hidden sql text
	InventorySQLHelper NotesData = new InventorySQLHelper(this);

	Boolean plus = false;
	Boolean minus = false;
	Boolean times = false;
	int answer = 0;
	String question = "";
	long beforeTime = 0;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		// grab a view to the image and load blank png
		ImageView imgView = (ImageView) findViewById(R.id.ImageView01);
		imgView.setImageResource(R.drawable.blankscreen);

		// photo on click listener
		imgView.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				randomMathProblemToEnter(v.getContext());

			}

		});

		/*
		 * // photo long click listener imgView.setOnLongClickListener(new
		 * OnLongClickListener() {
		 * 
		 * @Override public boolean onLongClick(View v) { // TODO Auto-generated
		 * method stub return true; } });
		 */
		// hidden button listener
		Button hiddenButton = (Button) findViewById(R.id.hideButton);
		hiddenButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				switchViews(v.getContext(), 0);
			}

		});

		// Look up the AdView as a resource and load a request.
		AdView adView = (AdView) this.findViewById(R.id.adView);
		adView.loadAd(new AdRequest());
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			if (requestCode == 1) {
				Uri selectedImageUri1 = data.getData();

				// grab a handle to the image
				ImageView imgPreView = (ImageView) findViewById(R.id.ImageView01);
				imgPreView.setImageURI(selectedImageUri1);

			}
		}
	}

	public void randomMathProblemToEnter(Context context) {
		Random myRand = new Random();
		int randomFirst = myRand.nextInt(100) + 1;
		int randomSecond = myRand.nextInt(10) + 1;
		String op = "";
		plus = false;
		minus = false;
		times = false; 
		if (randomFirst <= randomSecond) {
			randomFirst = randomSecond + 5; 
		}
		int operand = myRand.nextInt(3);

		switch (operand) {
		case 0:
			plus = true;
			op = "+";
			answer = randomFirst + randomSecond;
			break;
		case 1:
			minus = true;
			op = "-";
			answer = randomFirst - randomSecond;
			break;
		case 2:
			times = true;
			op = "*";
			randomFirst = myRand.nextInt(11) + 1;
			answer = randomFirst * randomSecond;
			break;
		default:
			break;
		}

		question = "What is " + randomFirst + " " + op + " " + randomSecond
				+ " ?";

		AlertDialog.Builder alert = new AlertDialog.Builder(context);

		alert.setTitle("Answer?");
		alert.setMessage(question);

		// Set an EditText view to get user input
		final EditText input = new EditText(context);
		// input.setText(lastHighScoreName);
		alert.setView(input);

		alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				String tempName = input.getText().toString().trim();
				int isAnswer = 0;
				if(tempName.length() > 0) {
				    try {
						isAnswer = Integer.valueOf(tempName);
					} catch (NumberFormatException e) {
						// TODO Auto-generated catch block
						Toast.makeText(getBaseContext(), "Is THAT Even A Number?!",
								Toast.LENGTH_SHORT).show();
						return;
					}
				} else {
					Toast.makeText(getBaseContext(), "At Least Try!",
							Toast.LENGTH_SHORT).show();
					return;
				}
				if (isAnswer == answer) {
					long aftertime = System.currentTimeMillis();
					if ((aftertime - beforeTime) < 8000) {
						dialog.dismiss();
						switchViews(getBaseContext(), 1);
					}
					else {
						Toast.makeText(getBaseContext(), "Too Slow!",
								Toast.LENGTH_SHORT).show();
						
						return;
					}
				} else {
					Toast.makeText(getBaseContext(), "Try Again!",
							Toast.LENGTH_SHORT).show();
					return;
				}
			}

		});

		alert.setNegativeButton("Cancel",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						// Canceled.
					}
				});
		beforeTime = System.currentTimeMillis();
		alert.show();
	}

	public void switchViews(Context context, int view) {
		ImageView imgView = (ImageView) findViewById(R.id.ImageView01);
		EditText hiddenText = (EditText) findViewById(R.id.hiddentext);
		Button hiddenButton = (Button) findViewById(R.id.hideButton);

		if (view == 1) {
			Cursor noteCursor = getNotesCursor();
			if (noteCursor.getCount() > 0) {
				noteCursor.moveToFirst();
				// retrieve our values for this row
				String Notes = noteCursor.getString(1);
				hiddenText.setText(Notes);
			}
			imgView.setVisibility(View.GONE);
			hiddenText.setVisibility(View.VISIBLE);
			hiddenButton.setVisibility(View.VISIBLE);
		} else {
			// here we save our hidden text to the database
			String name = "Unnamed Item";
			// now that we have a picture uri, create a new table entry for
			// this inventory item
			SQLiteDatabase db = NotesData.getWritableDatabase();
			ContentValues values = new ContentValues();
			values.put(InventorySQLHelper.NOTES, hiddenText.getText()
					.toString());
			long latestRowId = db
					.insert(InventorySQLHelper.TABLE, null, values);
			db.close();

			imgView.setVisibility(View.VISIBLE);
			hiddenText.setVisibility(View.GONE);
			hiddenButton.setVisibility(View.GONE);
		}

	}

	private Cursor getNotesCursor() {
		SQLiteDatabase db = NotesData.getReadableDatabase();
		Cursor cursor = db.query(InventorySQLHelper.TABLE, null, null, null,
				null, null, null);
		startManagingCursor(cursor);
		return cursor;
	}

}
package com.ssn.currency.convertor;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.xmlpull.v1.XmlPullParser;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.database.SQLException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Xml;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class CurrencyMain extends Activity {

	private boolean scrolling = false;
	ProgressDialog pd;

	DataBaseHelper myDbHelper = new DataBaseHelper(this);
	// static Button curr_select_1;
	// static Button curr_select_2;
	static TextView label_curr1;
	static TextView label_curr2;
	static WheelView left_curr_wheel;
	static WheelView right_curr_wheel;
	static Button update;
	static EditText input_text_value;
	static Dialog dialog;
	
	static TextView answer_text;
	final String[] currency_array = { "USD", "JPY", "BGN", "CZK", "DKK", "GBP",
			"HUF", "LTL", "LVL", "PLN", "RON", "SEK", "CHF", "NOK", "HRK",
			"RUB", "TRY", "AUD", "BRL", "CAD", "CNY", "HKD", "IDR", "ILS",
			"INR", "KRW", "MXN", "MYR", "NZD", "PHP", "SGD", "THB", "ZAR" };

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_currency_main);
		// curr_select_1=(Button)findViewById(R.id.Curr_select_button_1);
		// curr_select_2=(Button)findViewById(R.id.Curr_select_button_2);
		update = (Button) findViewById(R.id.update_button);
		input_text_value = (EditText) findViewById(R.id.inputText);
		answer_text = (TextView) findViewById(R.id.Answer_Text);
		label_curr1 = (TextView) findViewById(R.id.Curr1_Text);
		label_curr2 = (TextView) findViewById(R.id.Curr2_Text);
		
		
		dialog = new Dialog(this);

		

		

		

		input_text_value.addTextChangedListener(new TextWatcher() {

			public void afterTextChanged(Editable s) {
				s.toString();
				calculate_ans();

			}

			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub

			}

			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				// TODO Auto-generated method stub

			}

		});

		try {

			myDbHelper.createDataBase();

		} catch (IOException ioe) {

			throw new Error("Unable to create database");

		}

		try {

			myDbHelper.openDataBase();

		} catch (SQLException sqle) {

			throw sqle;

		}

		// ALL THE WHEEL STUFF IS BELOW
		left_curr_wheel = (WheelView) findViewById(R.id.curr1_wheel);
		left_curr_wheel.setViewAdapter(new ArrayWheelAdapter<String>(this,
				currency_array));
		left_curr_wheel.setCyclic(true);
		right_curr_wheel = (WheelView) findViewById(R.id.curr2_wheel);
		right_curr_wheel.setViewAdapter(new ArrayWheelAdapter<String>(this,
				currency_array));
		right_curr_wheel.setCyclic(true);
		left_curr_wheel.setCurrentItem(0, true);
		right_curr_wheel.setCurrentItem(0, true);

		label_curr1.setText(currency_array[left_curr_wheel.getCurrentItem()]);
		label_curr2.setText(currency_array[right_curr_wheel.getCurrentItem()]);

		left_curr_wheel.addChangingListener(new OnWheelChangedListener() {
			public void onChanged(WheelView wheel, int oldValue, int newValue) {
				if (!scrolling) {
					calculate_ans();
				}
			}
		});

		left_curr_wheel.addScrollingListener(new OnWheelScrollListener() {
			public void onScrollingStarted(WheelView wheel) {
				scrolling = true;
			}

			public void onScrollingFinished(WheelView wheel) {
				scrolling = false;
				calculate_ans();
			}
		});
		right_curr_wheel.addChangingListener(new OnWheelChangedListener() {
			public void onChanged(WheelView wheel, int oldValue, int newValue) {
				if (!scrolling) {
					calculate_ans();
				}
			}
		});

		right_curr_wheel.addScrollingListener(new OnWheelScrollListener() {
			public void onScrollingStarted(WheelView wheel) {
				scrolling = true;
			}

			public void onScrollingFinished(WheelView wheel) {
				scrolling = false;
				calculate_ans();
			}
		});

		// BELOW IS THE ONCLICK EVENT FOR UPDATE BUTTON
		update.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				// pd = ProgressDialog.show(CurrencyMain.this, "Working..",
				// "Fetching Values", false,true);
				
				ConnectivityManager connMgr = (ConnectivityManager) 
				        getSystemService(Context.CONNECTIVITY_SERVICE);
				    NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
				    if (networkInfo != null && networkInfo.isConnected()) {
				        // fetch data
				    	RetreiveFeedTask r = new RetreiveFeedTask();
						r.execute();
				    } else {
				    	Toast.makeText(getApplicationContext(), "Error: Unable to fetch data",
								Toast.LENGTH_SHORT).show();
				        // display error
				    }
				

			}
		});

		calculate_ans();

	} // END OF ONCREATE()

	public void calculate_ans() {
		String c1, c2;
		double c1_rate, c2_rate, input;
		float answer;

		c1 = currency_array[left_curr_wheel.getCurrentItem()];
		c2 = currency_array[right_curr_wheel.getCurrentItem()];
		label_curr1.setText(c1);
		label_curr2.setText(c2);
		c1_rate = myDbHelper.getRate(c1);
		c2_rate = myDbHelper.getRate(c2);
		try {
			input = Double.parseDouble(input_text_value.getText().toString());
		} catch (Exception e) {
			input = 0;
		}
		answer = (float) ((c2_rate * input) / c1_rate);
		answer = Round(answer, 2);
		answer_text.setText("=   "+answer);

	}

	public static float Round(float Rval, int Rpl) {
		float p = (float) Math.pow(10, Rpl);
		Rval = Rval * p;
		float tmp = Math.round(Rval);
		return (float) tmp / p;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_currency_main, menu);
		return true;
	}
	
	
	public boolean onOptionsItemSelected(MenuItem item) {
		dialog.setContentView(R.layout.dialogs);
		 TextView text = (TextView) dialog.findViewById(R.id.TextView01);
		 dialog.setCancelable(true);
		 Button  button = (Button ) dialog.findViewById(R.id.Button01);
         button.setOnClickListener(new OnClickListener() {
         
             public void onClick(View  v) {
                 dialog.dismiss();
             }
         });
		switch (item.getItemId()) {
		case R.id.How_To:
			dialog.setTitle("How To..");
            text.setText(R.string.how_to_text);
           dialog.show();

			return true;
			
		case R.id.Credits:
			
			dialog.setTitle("Credits..");
            text.setText(R.string.credit_text);
           dialog.show();

			return true;
			
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	class RetreiveFeedTask extends AsyncTask<String, Void, String> {
		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			// super.onPreExecute();
			pd = ProgressDialog.show(CurrencyMain.this, "Working..",
					"Fetching Values", false,true);

		}

		protected String doInBackground(String... params) {
			String xml = null;
			try {

				XmlPullParser parser = Xml.newPullParser();
				parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES,
						false);
				
				HttpGet method = new HttpGet(
						new URI(
								"http://www.ecb.int/stats/eurofxref/eurofxref-daily.xml"));
				HttpParams httpParameters = new BasicHttpParams();
				int timeoutConnection = 5000;
				HttpConnectionParams.setConnectionTimeout(httpParameters,
						timeoutConnection);
				int timeoutSocket = 5000;
				HttpConnectionParams
						.setSoTimeout(httpParameters, timeoutSocket);
				
				DefaultHttpClient client = new DefaultHttpClient(httpParameters);

				
				HttpResponse res = client.execute(method);

				InputStream is = res.getEntity().getContent();
				parser.setInput(is, null);

				// Parser is ready now
				int eventType = parser.getEventType();

				parser.nextTag();

				while (eventType != XmlPullParser.END_DOCUMENT) {
					if (eventType == XmlPullParser.START_TAG) {
						if (parser.getAttributeCount() == 2) {
							Log.v("SHASHANK",
									"ATT Value " + parser.getAttributeValue(1));
							myDbHelper.update(parser.getAttributeValue(0),
									parser.getAttributeValue(1));

						}
					}
					eventType = parser.next();
				}
				System.out.println("End document");

				xml = "Done, Database updated :)";
			} 

			
			catch (Exception e) {
				xml = "Error: Unable to fetch data";
				// pd.dismiss();
				e.printStackTrace();

			}
			return xml;
			
		}

		@Override
		protected void onPostExecute(String xml) {
pd.dismiss();
			Toast.makeText(CurrencyMain.this, xml, Toast.LENGTH_SHORT).show();
		}

	}

}

package com.cs365.uclick;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.cs365.uclick.data.Quiz;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class QuizActivity extends Activity implements OnClickListener,
		OnItemSelectedListener {
	private Spinner menu;
	List<String> quizList;
	//List<String> detailList;
	Map<String, List<String>> quizCollections;
	//ExpandableListView quizView;
	private EditText searchbox, quizbox;
	private Button search, start;
	private boolean tag;
	private ExpandableListView quizHistory;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.quiz);

		this.menu = (Spinner) this.findViewById(R.id.mainmenu);
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
				this, R.array.Menu, R.layout.spinnerstyle);
		adapter.setDropDownViewResource(R.layout.dropdown);
		menu.setAdapter(adapter);
		menu.setOnItemSelectedListener(this);
		tag = false;

		quizbox = (EditText) this.findViewById(R.id.qz_qid);
		start = (Button) this.findViewById(R.id.qz_start);

		searchbox = (EditText) this.findViewById(R.id.qz_sid);
		search = (Button) this.findViewById(R.id.qz_search);

		search.setOnClickListener(this);
		start.setOnClickListener(this);
		createCollection();

	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if (v == search) {

		} else if (v == start) {
			final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
			final Intent intent = new Intent(this, ClickerActivity.class);
			final String quizID = quizbox.getText().toString();
			ParseQuery<ParseObject> query = new ParseQuery<ParseObject>("Quiz");
			query.whereEqualTo(MyData.QUIZ_ID, quizID);

			query.getFirstInBackground(new GetCallback<ParseObject>() {

				@Override
				public void done(ParseObject quiz, ParseException e) {
					// TODO Auto-generated method stub
					if (quiz != null) {
						Quiz q = new Quiz();
						q.setId(quiz.getString(MyData.QUIZ_ID));
						q.setDesc(quiz.getString(MyData.DESCRIPTION));
						q.setQuestions(quiz.getInt(MyData.QUIZ_N));
						ArrayList<String> sols = (ArrayList<String>) quiz
								.get(MyData.QUIZ_SOLUTIONS);
						q.setAnswers(sols);
						Log.d("siiiiiiiiiiiiize", "" + sols.size());
						for (int i = 0; i < sols.size(); i++)
							Log.d("answeeeer", "" + sols.get(i));
						MyData.quiz = q;
						startActivity(intent);

					} else {
						dialog.setTitle("ERROR");
						dialog.setMessage("Enter a valid Quiz ID");

						dialog.setIcon(R.drawable.ic_launcher);

						dialog.setNeutralButton("OK",
								new DialogInterface.OnClickListener() {

									public void onClick(DialogInterface dialog,
											int which) {
										dialog.dismiss();

									}
								});

						dialog.show();

					}

				}
			});
		}

	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int pos,
			long id) {
		// TODO Auto-generated method stub
		TextView v = (TextView) view;

		if (tag) {
			if (v.getText().equals("YOUR ACCOUNT")) {
				Intent intent = new Intent(this, ProfileActivity.class);
				startActivity(intent);

			} else if (v.getText().equals("SIGN OUT")) {
				final Intent intent = new Intent(this, LoginActivity.class);
				AlertDialog.Builder dialog = new AlertDialog.Builder(this);

				dialog.setTitle("Log Out");
				dialog.setMessage("Are you sure you want to log out?");

				dialog.setIcon(R.drawable.ic_launcher);

				dialog.setPositiveButton("Yes",
						new DialogInterface.OnClickListener() {

							public void onClick(DialogInterface dialog,
									int which) {
								ParseUser.logOut();
								MyData.quiz = null;
								MyData.usr = null;
								startActivity(intent);
							}
						});
				dialog.setNegativeButton("No",
						new DialogInterface.OnClickListener() {

							public void onClick(DialogInterface dialog,
									int which) {
								dialog.dismiss();
							}
						});

				dialog.show();
			}
		}
		tag = true;

	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		// TODO Auto-generated method stub

	}

	private void createCollection() {

		quizCollections = new LinkedHashMap<String, List<String>>();
		quizList = new ArrayList<String>();

		ParseQuery<ParseObject> query = new ParseQuery<ParseObject>(
				"UserQuizList");
		query.whereEqualTo(MyData.QUIZ_USER, ParseUser.getCurrentUser());
		query.findInBackground(new FindCallback<ParseObject>() {

			@Override
			public void done(List<ParseObject> lists, ParseException e) {
				// TODO Auto-generated method stub
				if (lists != null) {
					for (int i = 0; i < lists.size(); i++) {
						final ParseObject currentList = lists.get(i);
						ParseObject currentQuiz = currentList
								.getParseObject(MyData.QUIZ_QUIZ);
						ParseQuery<ParseObject> quizes = new ParseQuery<ParseObject>(
								"Quiz");
						quizes.getInBackground(currentQuiz.getObjectId(),
								new GetCallback<ParseObject>() {

									@Override
									public void done(ParseObject q,
											ParseException e) {
										// TODO Auto-generated method stub
										final List<String> detailList = new ArrayList<String>();
										String date = "Date: "
												+ currentList.getCreatedAt()
														.toLocaleString();
										detailList.add(date);
										String description = "Description: "
												+ q.getString(MyData.DESCRIPTION);
										detailList.add(description);
										String result = "Points: "
												+ currentList
														.getInt(MyData.QUIZ_RESULT)
												+ "/" + q.getInt(MyData.QUIZ_N);
										detailList.add(result);
										ParseQuery<ParseObject> subjs = new ParseQuery<ParseObject>(
												"Subject");
										subjs.getInBackground(
												q.getParseObject(
														MyData.QUIZ_SUBJECT)
														.getObjectId(),
												new GetCallback<ParseObject>() {

													@Override
													public void done(
															ParseObject subj,
															ParseException e) {
														// TODO Auto-generated
														// method stub
														String subjName = "Subject: "
																+ subj.getString(MyData.SUBJ_NAME);
														detailList
																.add(subjName);
														ParseQuery<ParseObject> insts = new ParseQuery<ParseObject>(
																"Instructor");
														insts.getInBackground(
																subj.getParseObject(
																		"toughtBy")
																		.getObjectId(),
																new GetCallback<ParseObject>() {

																	@Override
																	public void done(
																			ParseObject inst,
																			ParseException e) {
																		// TODO
																		// Auto-generated
																		// method
																		// stub
																		String instructor = "Professor: "
																				+ inst.getString(MyData.USR_FIRST_NAME)
																				+ " "
																				+ inst.getString(MyData.USR_LAST_NAME);
																		detailList
																				.add(instructor);
																	}
																});
													}
												});
										quizCollections.put(
												q.getString(MyData.QUIZ_ID),
												detailList);
										quizList.add(q
												.getString(MyData.QUIZ_ID));
									}

								});

					}

				}

			}

		});
		quizHistory = (ExpandableListView) findViewById(R.id.qz_qzlist);
		ExpandibleListAdapter expListAdapter = new ExpandibleListAdapter(this,
				quizList, quizCollections);
		quizHistory.setAdapter(expListAdapter);

	}
}
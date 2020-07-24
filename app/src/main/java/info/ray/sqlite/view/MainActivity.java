package info.ray.sqlite.view;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.VolleyError;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import info.ray.sqlite.R;
import info.ray.sqlite.database.DatabaseHelper;
import info.ray.sqlite.database.model.Note;
import info.ray.sqlite.utils.IResult;
import info.ray.sqlite.utils.MyDividerItemDecoration;
import info.ray.sqlite.utils.RecyclerTouchListener;
import info.ray.sqlite.utils.Utils;
import info.ray.sqlite.utils.VolleyService;

import static java.lang.Thread.sleep;

public class MainActivity extends AppCompatActivity implements IResult{

    private String TAG="MainActivity";
    private NotesAdapter mAdapter;
    private List<Note> notesList = new ArrayList<>();
    private CoordinatorLayout coordinatorLayout;
    private RecyclerView recyclerView;
    private TextView noNotesView;

    private DatabaseHelper db;

    private VolleyService mVolleyService;
    private IResult mResultCallback = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        coordinatorLayout = findViewById(R.id.coordinator_layout);
        recyclerView = findViewById(R.id.recycler_view);
        noNotesView = findViewById(R.id.empty_notes_view);

        db = new DatabaseHelper(this);

        //notesList.addAll(db.getAllNotes());


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showNoteDialog(false, null, -1);
            }
        });

        mAdapter = new NotesAdapter(this, notesList);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new MyDividerItemDecoration(this, LinearLayoutManager.VERTICAL, 16));
        recyclerView.setAdapter(mAdapter);
        getAllNotes();
        toggleEmptyNotes();

        /**
         * On long press on RecyclerView item, open alert dialog
         * with options to choose
         * Edit and Delete
         * */
        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(this,
                recyclerView, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, final int position) {
            }

            @Override
            public void onLongClick(View view, int position) {
                showActionsDialog(position);
            }
        }));
    }

    /**
     * Inserting new note in db
     * and refreshing the list
     */
    private void createNote(String note) {
        // inserting note in db and getting
        // newly inserted note id
        long id = db.insertNote(note);

        // get the newly inserted note from db
        Note n = db.getNote(id);

        if (n != null) {
            // adding new note to array list at 0 position
            notesList.add(0, n);

            // refreshing the list
            mAdapter.notifyDataSetChanged();

            toggleEmptyNotes();
        }
    }

    /**
     * Updating note in db and updating
     * item in the list by its position
     */
    private void updateNote(String note, int position) {
        Note n = notesList.get(position);
        // updating note text
        n.setNote(note);

        // updating note in db
        db.updateNote(n);

        // refreshing the list
        notesList.set(position, n);
        mAdapter.notifyItemChanged(position);

        toggleEmptyNotes();
    }

    /**
     * Deleting note from SQLite and removing the
     * item from the list by its position
     */
    private void deleteNote(int position) {
        // deleting the note from db
        db.deleteNote(notesList.get(position));

        // removing the note from the list
        notesList.remove(position);
        mAdapter.notifyItemRemoved(position);

        toggleEmptyNotes();
    }

    /**
     * Opens dialog with Edit - Delete options
     * Edit - 0
     * Delete - 0
     */
    private void showActionsDialog(final int position) {
        CharSequence colors[] = new CharSequence[]{"Edit", "Delete"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose option");
        builder.setItems(colors, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    showNoteDialog(true, notesList.get(position), position);
                } else {
                    //deleteNote(position);
                    deleteNote(notesList.get(position));
                }
            }
        });
        builder.show();
    }


    /**
     * Shows alert dialog with EditText options to enter / edit
     * a note.
     * when shouldUpdate=true, it automatically displays old note and changes the
     * button text to UPDATE
     */
    private void showNoteDialog(final boolean shouldUpdate, final Note note, final int position) {
        LayoutInflater layoutInflaterAndroid = LayoutInflater.from(getApplicationContext());
        View view = layoutInflaterAndroid.inflate(R.layout.note_dialog, null);

        AlertDialog.Builder alertDialogBuilderUserInput = new AlertDialog.Builder(MainActivity.this);
        alertDialogBuilderUserInput.setView(view);

        final EditText inputNote = view.findViewById(R.id.note);
        TextView dialogTitle = view.findViewById(R.id.dialog_title);
        dialogTitle.setText(!shouldUpdate ? getString(R.string.lbl_new_note_title) : getString(R.string.lbl_edit_note_title));

        if (shouldUpdate && note != null) {
            inputNote.setText(note.getNote());
        }
        alertDialogBuilderUserInput
                .setCancelable(false)
                .setPositiveButton(shouldUpdate ? "update" : "save", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogBox, int id) {

                    }
                })
                .setNegativeButton("cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialogBox, int id) {
                                dialogBox.cancel();
                            }
                        });

        final AlertDialog alertDialog = alertDialogBuilderUserInput.create();
        alertDialog.show();

        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Show toast message when no text is entered
                if (TextUtils.isEmpty(inputNote.getText().toString())) {
                    Toast.makeText(MainActivity.this, "Enter note!", Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    alertDialog.dismiss();
                }

                // check if user updating note
                if (shouldUpdate && note != null) {
                    // update note by it's id
                    //updateNote(inputNote.getText().toString(), position);
                    updateNote(inputNote.getText().toString(),notesList.get(position));
                } else {
                    // create new note
                    //createNote(inputNote.getText().toString());
                    addNewNote(inputNote.getText().toString());
                }
            }
        });
    }

    /**
     * Toggling list and empty notes view
     */
    private void toggleEmptyNotes() {
        // you can check notesList.size() > 0

        if (notesList.size() > 0) {
            noNotesView.setVisibility(View.GONE);
        } else {
            noNotesView.setVisibility(View.VISIBLE);
        }
    }



    /**
     * Method will Fetch Data from API
     */
    /**
     * Method will Fetch/Delete Stored Records from Database
     */
    private void getAllNotes() {
        if (Utils.isOnline(MainActivity.this)) {
            Log.d(TAG,"getAllNotes()");
            mResultCallback = new IResult() {
                @Override
                public void notifySuccess(String requestType, String response) {
                    Log.d(TAG, "Volley JSON post" + response);


                    try {
                        if(requestType.equalsIgnoreCase("get_all_notes")){

                            JSONArray responseArray=new JSONArray(response);
                            Log.d(TAG,responseArray.toString());
                            if (responseArray.length() > 0) {
                                for (int i = 0; i < responseArray.length(); i++) {
                                    JSONObject responseObject=responseArray.getJSONObject(i);
                                    Note note = new Note();
                                    note.setId(Integer.valueOf(responseObject.get("id").toString()));
                                    note.setNote(responseObject.get("note").toString());
                                    note.setTimestamp(responseObject.get("timestamp").toString());
                                    notesList.add(note);
                                }

                                mAdapter.notifyDataSetChanged();
                                toggleEmptyNotes();
                            } else {
                                Toast.makeText(MainActivity.this, "No Notes Found", Toast.LENGTH_SHORT).show();
                            }

                        }
                    } catch (Exception je) {
                        Log.e(TAG, je.toString());
                    }
                }

                @Override
                public void notifyError(String requestType, VolleyError error) {
                    Log.d(TAG, "Volley requester " + requestType);
                    Log.d(TAG, "Volley JSON post" + "That didn't work!");
                    Log.e(TAG, error.toString());
                }
            };
            //mResultCallback = new MainActivity();
            mVolleyService = new VolleyService(mResultCallback, MainActivity.this);

            JSONObject sendObj = new JSONObject();
            try {
                sendObj.put("ACTION", "get_all_notes");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            mVolleyService.postDataVolley("get_all_notes", Utils.SERVER_HOME_URL, sendObj);

        } else {
            Toast.makeText(MainActivity.this,
                    getResources().getString(R.string.network_error),
                    Toast.LENGTH_LONG).show();
        }
    }


    private void addNewNote(String newNote) {
        if (Utils.isOnline(MainActivity.this)) {
            Log.d(TAG,"AddNewNote()");
            mResultCallback = new IResult() {
                @Override
                public void notifySuccess(String requestType, String response) {
                    Log.d(TAG, "Volley JSON post" + response);


                    try {
                        if(requestType.equalsIgnoreCase("add_note")){

                            JSONArray responseArray=new JSONArray(response);
                            Log.d(TAG,responseArray.toString());
                            notesList.clear();
                            if (responseArray.length() > 0) {
                                for (int i = 0; i < responseArray.length(); i++) {
                                    JSONObject responseObject=responseArray.getJSONObject(i);
                                    Note note = new Note();
                                    note.setId(Integer.valueOf(responseObject.get("id").toString()));
                                    note.setNote(responseObject.get("note").toString());
                                    note.setTimestamp(responseObject.get("timestamp").toString());
                                    notesList.add(note);
                                }

                                mAdapter.notifyDataSetChanged();
                                toggleEmptyNotes();
                            } else {
                                Toast.makeText(MainActivity.this, "No Notes Found", Toast.LENGTH_SHORT).show();
                            }

                        }
                    } catch (Exception je) {
                        Log.e(TAG, je.toString());
                    }
                }

                @Override
                public void notifyError(String requestType, VolleyError error) {
                    Log.d(TAG, "Volley requester " + requestType);
                    Log.d(TAG, "Volley JSON post" + "That didn't work!");
                    Log.e(TAG, error.toString());
                }
            };
            //mResultCallback = new MainActivity();
            mVolleyService = new VolleyService(mResultCallback, MainActivity.this);

            JSONObject sendObj = new JSONObject();
            try {
                sendObj.put("ACTION", "add_note");
                sendObj.put("note", newNote);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            mVolleyService.postDataVolley("add_note", Utils.SERVER_HOME_URL, sendObj);

        } else {
            Toast.makeText(MainActivity.this,
                    getResources().getString(R.string.network_error),
                    Toast.LENGTH_LONG).show();
        }
    }

    private void updateNote(String newNote, Note note){
        if (Utils.isOnline(MainActivity.this)) {
            Log.d(TAG,"updateNote()");
            mResultCallback = new IResult() {
                @Override
                public void notifySuccess(String requestType, String response) {
                    Log.d(TAG, "Volley JSON post" + response);


                    try {
                        if(requestType.equalsIgnoreCase("update_note")){

                            JSONArray responseArray=new JSONArray(response);
                            Log.d(TAG,responseArray.toString());
                            notesList.clear();
                            if (responseArray.length() > 0) {
                                for (int i = 0; i < responseArray.length(); i++) {
                                    JSONObject responseObject=responseArray.getJSONObject(i);
                                    Note note = new Note();
                                    note.setId(Integer.valueOf(responseObject.get("id").toString()));
                                    note.setNote(responseObject.get("note").toString());
                                    note.setTimestamp(responseObject.get("timestamp").toString());
                                    notesList.add(note);
                                }

                                mAdapter.notifyDataSetChanged();
                                toggleEmptyNotes();
                            } else {
                                Toast.makeText(MainActivity.this, "No Notes Found", Toast.LENGTH_SHORT).show();
                            }

                        }
                    } catch (Exception je) {
                        Log.e(TAG, je.toString());
                    }
                }

                @Override
                public void notifyError(String requestType, VolleyError error) {
                    Log.d(TAG, "Volley requester " + requestType);
                    Log.d(TAG, "Volley JSON post" + "That didn't work!");
                    Log.e(TAG, error.toString());
                }
            };
            //mResultCallback = new MainActivity();
            mVolleyService = new VolleyService(mResultCallback, MainActivity.this);

            JSONObject sendObj = new JSONObject();
            try {
                sendObj.put("ACTION", "update_note");
                sendObj.put("note", newNote);
                sendObj.put("id", note.getId());
            } catch (JSONException e) {
                e.printStackTrace();
            }

            mVolleyService.postDataVolley("update_note", Utils.SERVER_HOME_URL, sendObj);

        } else {
            Toast.makeText(MainActivity.this,
                    getResources().getString(R.string.network_error),
                    Toast.LENGTH_LONG).show();
        }
    }

    private void deleteNote(Note note){
        if (Utils.isOnline(MainActivity.this)) {
            Log.d(TAG,"deleteNote()");
            mResultCallback = new IResult() {
                @Override
                public void notifySuccess(String requestType, String response) {
                    Log.d(TAG, "Volley JSON post" + response);


                    try {
                        if(requestType.equalsIgnoreCase("delete_note")){

                            JSONArray responseArray=new JSONArray(response);
                            Log.d(TAG,responseArray.toString());
                            notesList.clear();
                            if (responseArray.length() > 0) {
                                for (int i = 0; i < responseArray.length(); i++) {
                                    JSONObject responseObject=responseArray.getJSONObject(i);
                                    Note note = new Note();
                                    note.setId(Integer.valueOf(responseObject.get("id").toString()));
                                    note.setNote(responseObject.get("note").toString());
                                    note.setTimestamp(responseObject.get("timestamp").toString());
                                    notesList.add(note);
                                }

                                mAdapter.notifyDataSetChanged();
                                toggleEmptyNotes();
                            } else {
                                Toast.makeText(MainActivity.this, "No Notes Found", Toast.LENGTH_SHORT).show();
                            }

                        }
                    } catch (Exception je) {
                        Log.e(TAG, je.toString());
                    }
                }

                @Override
                public void notifyError(String requestType, VolleyError error) {
                    Log.d(TAG, "Volley requester " + requestType);
                    Log.d(TAG, "Volley JSON post" + "That didn't work!");
                    Log.e(TAG, error.toString());
                }
            };
            //mResultCallback = new MainActivity();
            mVolleyService = new VolleyService(mResultCallback, MainActivity.this);

            JSONObject sendObj = new JSONObject();
            try {
                sendObj.put("ACTION", "delete_note");
                sendObj.put("id", note.getId());
            } catch (JSONException e) {
                e.printStackTrace();
            }

            mVolleyService.postDataVolley("delete_note", Utils.SERVER_HOME_URL, sendObj);

        } else {
            Toast.makeText(MainActivity.this,
                    getResources().getString(R.string.network_error),
                    Toast.LENGTH_LONG).show();
        }
    }



    @Override
    public void notifySuccess(String requestType, String response) {
        Log.d(TAG, "Volley JSON post" + response);


        try {
            if(requestType.equalsIgnoreCase("get_all_notes")){

                JSONArray responseArray=new JSONArray(response);
            Log.d(TAG,responseArray.toString());
            if (responseArray.length() > 0) {
                for (int i = 0; i < responseArray.length(); i++) {
                    JSONObject responseObject=responseArray.getJSONObject(i);
                    Note note = new Note();
                    note.setId(Integer.valueOf(responseObject.get("id").toString()));
                    note.setNote(responseObject.get("note").toString());
                    note.setTimestamp(responseObject.get("timestamp").toString());
                    notesList.add(note);
                }

                mAdapter.notifyDataSetChanged();
            } else {
                Toast.makeText(MainActivity.this, "No Notes Found", Toast.LENGTH_SHORT).show();
            }

        }
        } catch (Exception je) {
            Log.e(TAG, je.toString());
        }
    }

    @Override
    public void notifyError(String requestType, VolleyError error) {
        Log.d(TAG, "Volley requester " + requestType);
        Log.d(TAG, "Volley JSON post" + "That didn't work!");
        Log.e(TAG, error.toString());
    }
}

package com.couchbase.todolite;

import android.app.ListActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Document;
import com.couchbase.lite.SavedRevision;
import com.couchbase.lite.UnsavedRevision;
import com.couchbase.lite.util.Log;
import com.couchbase.todolite.Application;
import com.couchbase.todolite.ConflictsAdapter;

import java.util.ArrayList;
import java.util.List;


public class ListConflicts extends ListActivity {

    private ArrayList<SavedRevision> conflicts;
    private Document task;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*
        Get the task document with conflicting revisions
        and display them on the list view.
         */
        String id = (String) getIntent().getExtras().get("DOC_ID");
        final Application application = (Application) getApplication();
        task = application.getDatabase().getDocument(id);

        try {
            conflicts = new ArrayList<SavedRevision>(task.getConflictingRevisions());
        } catch (CouchbaseLiteException e) {
            Log.e(Application.TAG, "Cannot get the conflicting revisions");
        }

        ConflictsAdapter adapter = new ConflictsAdapter(this, conflicts);
        this.setListAdapter(adapter);

        getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                SavedRevision selectedRevision = conflicts.get(i);
                SavedRevision currentRevision = task.getCurrentRevision();

                /*
                Create a new revision with the properties of the revision selected by
                the user and delete all other revisions.
                 */
                for (SavedRevision rev : conflicts) {
                    try {
                        UnsavedRevision newRev = rev.createRevision();
                        if (rev == currentRevision) {
                            newRev.setUserProperties(selectedRevision.getUserProperties());
                        } else {
                            newRev.setIsDeletion(true);
                        }
                        newRev.save(true);
                    } catch (CouchbaseLiteException e) {
                        Log.e(Application.TAG, "Cannot create a new revision", e);
                    }
                }

                /*
                Return to list page, it should be updated with the desired properties
                and the conflict icon should be hidden
                 */
                finish();

            }
        });

    }
}

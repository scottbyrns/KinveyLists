package com.kinvey.android.lists;

import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.kinvey.KCSClient;
import com.kinvey.MappedAppdata;
import com.kinvey.android.lists.entities.ListEntity;
import com.kinvey.android.lists.entities.ListItemEntity;
import com.kinvey.util.ListCallback;
import com.kinvey.util.ScalarCallback;

public final class MainActivity extends Activity {
    public static final String LIST_ITEM_COLLECTION = "listItemCollection";

	public static final String LIST_COLLECTION = "listCollection";

	private static final String TAG = "MainActivity";

    private ListView mListView;
    private Button mButton;
    private List<ListEntity> listData;

    private KCSClient sharedClient;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.v(TAG, "Activity State: onCreate()");
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main);

        try {
            sharedClient = ((KinveyListsApp) getApplication()).getKinveyService();

            mListView = (ListView) findViewById(R.id.listOfLists);
            mButton = (Button) findViewById(R.id.new_list_button);

            // Register UI handlers
            mButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    Intent intent = new Intent(getApplicationContext(), AddEditListActivity.class);
                    intent.putExtra("action", "new");

                    startActivity(intent);
                }
            });

            // Register UI handler for list item selection
            mListView.setOnItemClickListener(new OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int arg2, long arg3) {
                    ListEntity sel = (ListEntity) adapterView.getItemAtPosition(arg2);

                    Intent intent = new Intent(MainActivity.this, ViewListDetailActivity.class);
                    intent.putExtra("listId", sel.getId());
                    startActivity(intent);

                }
            });

            registerForContextMenu(mListView);

        } catch (RuntimeException re) {
            Log.e(TAG, "Error occurred in onCreate()", re);
            displayError(re);
        }
    }

    /*
     * (non-Javadoc)
     * @see android.app.Activity#onResume()
     */
    @Override
    protected void onResume() {
        super.onResume();

        /*
         * Reload list data from server whenever activity resumes. Ideally this would probably be cached somewhere and
         * checks would be made for staleness to avoid unnecessary network requests
         */
        populateListViewWithListEntities();
    }

    public void populateListViewWithListEntities() {
        Log.i(TAG, "Populating parent list view from Kinvey service using MappedClass");
        final ProgressDialog progressDialog = ProgressDialog.show(MainActivity.this, "", "Loading. Please wait...",
                true);

        sharedClient.mappeddata(LIST_COLLECTION).all(ListEntity.class, new ListCallback<ListEntity>() {

            @Override
            public void onSuccess(List<ListEntity> result) {
                Log.d(TAG, "Received success response");
                listData = result;
                mListView.setAdapter(new ArrayAdapter<ListEntity>(MainActivity.this,
                        android.R.layout.simple_list_item_1, result));
                progressDialog.dismiss();

                // Toast.makeText(getApplicationContext(), "Successful retrieval!", Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onFailure(Throwable error) {
                Log.e(TAG, "Received error response", error);
                displayError(error);
                progressDialog.dismiss();
            }

        });

    }

    private void displayError(Throwable error) {
        Log.e(TAG, "Unexpected error occurred", error);
        TextView tv = new TextView(this);
        tv.setText(error.toString() + " - see the logs for more details");
        setContentView(tv);
    }

    /*
     * (non-Javadoc)
     * @see android.app.Activity#onCreateContextMenu(android.view.ContextMenu, android.view.View,
     * android.view.ContextMenu.ContextMenuInfo)
     */
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        if (v.getId() == R.id.listOfLists) {
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
            menu.setHeaderTitle(listData.get(info.position).toString());
            String[] menuItems = getResources().getStringArray(R.array.list_context_menu_items);
            for (int i = 0; i < menuItems.length; i++) {
                menu.add(Menu.NONE, i, i, menuItems[i]);
            }
        }
    }

    /*
     * (non-Javadoc)
     * @see android.app.Activity#onContextItemSelected(android.view.MenuItem)
     */
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        int menuItemId = item.getItemId();
        String[] menuItems = getResources().getStringArray(R.array.list_context_menu_items);
        String action = menuItems[menuItemId];
        ListEntity activeEntity = listData.get(((AdapterContextMenuInfo) item.getMenuInfo()).position);
        if ("Edit".equals(action)) {
            editListItem(activeEntity);
        } else if ("Delete".equals(action)) {
            deleteListItem(activeEntity);
        } else {
            Log.e(TAG, "Invalid context menu item selected!!!");
        }

        return true;
    }

    private void deleteListItem(ListEntity activeEntity) {
        Log.i(TAG, "Attempting to delete list entity: " + activeEntity.toString());
        final ProgressDialog pd = ProgressDialog.show(this, "Deleting", "Please wait...", true);
        final String listId = activeEntity.getId();
        sharedClient.mappeddata(LIST_COLLECTION).delete(activeEntity.getId(), new ScalarCallback<Void>() {

            @Override
            public void onSuccess(Void r) {
                pd.dismiss();
                Toast.makeText(MainActivity.this, "List deleted!", Toast.LENGTH_SHORT).show();
                // Call to delete children can happen without user feedback as there should be no way of
                // accessing the items if the parent list entry is deleted. Only show feedback on error
                cascadeDeleteListItems(listId);
                populateListViewWithListEntities();
            }
        });
    }

    protected void cascadeDeleteListItems(String listId) {
        Log.i(TAG, "Attempting to delete list items for list with id: " + listId);
        MappedAppdata coll = sharedClient.mappeddata(LIST_ITEM_COLLECTION);

        final String tempListId = listId;
        coll.addFilterCriteria("owner", "==", listId);
        coll.fetchByFilterCriteria(ListItemEntity.class, new ListCallback<ListItemEntity>() {

            @Override
            public void onSuccess(List<ListItemEntity> result) {
                Log.i(TAG, String.format("Got %d items for list %s", result.size(), tempListId));
                for (ListItemEntity item : result) {
                    deleteSingleItem(item);
                }
            }

            @Override
            public void onFailure(Throwable e) {
                Log.e(TAG, "Received error response while trying to fetch list items", e);
                displayError(e);
            }

        });

    }

    protected void deleteSingleItem(ListItemEntity item) {
        final String itemId = item.getId();
        sharedClient.mappeddata(LIST_ITEM_COLLECTION).delete(itemId, new ScalarCallback<Void>() {

            @Override
            public void onSuccess(Void r) {
                Log.d(TAG, "Successful delete of single item: " + itemId);

            }

            @Override
            public void onFailure(Throwable e) {
                Log.e(TAG, "Error while deleting single item: ", e);
            }
        });

    }

    private void editListItem(ListEntity activeEntity) {
        Log.i(TAG, "About to edit list entity: " + activeEntity.getId());
        Intent intent = new Intent(getApplicationContext(), AddEditListActivity.class);
        intent.putExtra("action", "edit");
        intent.putExtra("listId", activeEntity.getId());
        intent.putExtra("listName", activeEntity.getName());
        startActivity(intent);

    }

}
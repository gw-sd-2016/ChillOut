package com.example.grayapps.contextaware;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.TextView;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Large screen devices (such as tablets) are supported by replacing the ListView
 * with a GridView.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnFragmentInteractionListener}
 * interface.
 */
public class EventAttendeesListFragment extends ListFragment implements SearchView.OnQueryTextListener, LoaderManager.LoaderCallbacks<Cursor> {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private String mCurFilter;
    private static final int LOADER_EVENTS = 1;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    /**
     * The fragment's ListView/GridView.
     */
    private AbsListView mListView;

    /**
     * The Adapter which will be used to populate the ListView/GridView with
     * Views.
     */
    private SimpleCursorAdapter mAdapter;

    // TODO: Rename and change types of parameters
    public static EventAttendeesListFragment newInstance(String param1, String param2) {
        EventAttendeesListFragment fragment = new EventAttendeesListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public EventAttendeesListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            // Activity activity = (Activity) context;
            mListener = (OnFragmentInteractionListener) getActivity();

        } catch (ClassCastException e) {
            throw new ClassCastException(getActivity().toString()
                    + " must implement OnFragmentInteractionListener");
        }
        if(null != mListener)
            Log.d("Listener", "Is not null");
        else
            Log.d("Listener", "Is null");
        String from[] = new String[]{CalendarContract.Attendees.ATTENDEE_NAME};
        int to[] = {R.id.attendeeName};

        getLoaderManager().initLoader(LOADER_EVENTS, getArguments(), this);

        mAdapter = new SimpleCursorAdapter(getActivity(), R.layout.eventattendee_list_item, null, from, to, CursorAdapter.NO_SELECTION);
        setListAdapter(mAdapter);
        setHasOptionsMenu(true);
    }

    /**/@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_event_attendees_list, container, false);

        // Set the adapter
        mListView = (AbsListView) view.findViewById(android.R.id.list);

        mListView.setAdapter(mAdapter);

        // Set OnItemClickListener so we can be notified on item clicks
      //  mListView.setOnItemClickListener(this);
        Log.d("IsClickable", "" + mListView.isClickable());
        Log.d("CreatedView", "MADE IT");
        return view;
    }

    @Override
    public void onAttach(Context context) {
        Log.d("OnAttach", "Is being called.");
        super.onAttach(context);
        try {
            Activity activity = (Activity) context;
            mListener = (OnFragmentInteractionListener) activity;

        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * The default content for this Fragment has a TextView that is shown when
     * the list is empty. If you would like to change the text, call this method
     * to supply the text it should use.
     */
    public void setEmptyText(CharSequence emptyText) {
        View emptyView = mListView.getEmptyView();

        if (emptyView instanceof TextView) {
            ((TextView) emptyView).setText(emptyText);
        }
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(String id);
    }

    public boolean onQueryTextChange(String newText) {
        // Called when the action bar search text has changed.  Update
        // the search filter, and restart the loader to do a new query
        // with this filter.
        mCurFilter = !TextUtils.isEmpty(newText) ? newText : null;
        getLoaderManager().restartLoader(0, null, this);
        return true;
    }

    @Override public boolean onQueryTextSubmit(String query) {
        // Don't care about this.
        return true;
    }

    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case (LOADER_EVENTS):
                Uri uri = CalendarContract.Attendees.CONTENT_URI;
                String[] projection = new String[] {
                        CalendarContract.Attendees._ID,
                        CalendarContract.Attendees.EVENT_ID,
                        CalendarContract.Attendees.ATTENDEE_NAME
                };

                long eventId = getActivity().getPreferences(Context.MODE_PRIVATE).getLong("lastAccessedEventId", -1);
                Log.d("EventId", "" + eventId);

                String query = CalendarContract.Attendees.EVENT_ID + " = ?";

                return new CursorLoader(getActivity(), uri, projection, query, new String[] {String.valueOf(eventId)}, CalendarContract.Attendees.ATTENDEE_NAME + " ASC");

        }

        return null;
    }

    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Swap the new cursor in.  (The framework will take care of closing the
        // old cursor once we return.)

        Log.d("Attendee Count", "" + data.getCount());
        if(data.getCount() == 0)
        {
            TextView guestText = (TextView) getActivity().findViewById(R.id.attendeesText);
            guestText.setVisibility(View.INVISIBLE);
        }
        mAdapter.swapCursor(data);
    }

    public void onLoaderReset(Loader<Cursor> loader) {
        // This is called when the last Cursor provided to onLoadFinished()
        // above is about to be closed.  We need to make sure we are no
        // longer using it.
        mAdapter.swapCursor(null);
    }

}

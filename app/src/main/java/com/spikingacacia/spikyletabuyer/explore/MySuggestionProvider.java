package com.spikingacacia.spikyletabuyer.explore;

import android.app.SearchManager;
import android.content.ContentValues;
import android.content.SearchRecentSuggestionsProvider;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.spikingacacia.spikyletabuyer.database.Restaurants;


public class MySuggestionProvider extends SearchRecentSuggestionsProvider
{
    public final static String AUTHORITY = "com.spikingacacia.MySuggestionProvider";
    public final static int MODE = DATABASE_MODE_QUERIES | DATABASE_MODE_2LINES;
    private static final String RESTAURANTS = "restaurants/"+ SearchManager.SUGGEST_URI_PATH_QUERY+"/*";

    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        uriMatcher.addURI("com.spikingacacia.MySuggestionProvider", RESTAURANTS, 1);
    }

    private static String[] matrixCursorColumns = {
            "_id",
            SearchManager.SUGGEST_COLUMN_TEXT_1,
            SearchManager.SUGGEST_COLUMN_TEXT_2,
            SearchManager.SUGGEST_COLUMN_ICON_1,
            SearchManager.SUGGEST_COLUMN_INTENT_DATA};

    public MySuggestionProvider() {
        setupSuggestions(AUTHORITY, MODE);
    }
    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {

        String queryType = "";
        switch(uriMatcher.match(uri)){
            case 1 :
                String query = uri.getLastPathSegment().toLowerCase();
                return getSearchResultsCursor(query);
            default:
                return null;
        }
    }

    private MatrixCursor getSearchResultsCursor(String searchString){
        MatrixCursor searchResults =  new MatrixCursor(matrixCursorColumns);
        Object[] mRow = new Object[5];
        int counterId = 0;
        if(searchString != null)
        {
            searchString = searchString.toLowerCase();

            for(String rec :  RestaurantsData.getRestaurants())
            {
                if(rec.toLowerCase().contains(searchString))
                {
                    String[] name_and_id = rec.split(":");
                    mRow[0] = ""+counterId++;
                    mRow[1] = name_and_id[0];
                    Restaurants restaurants = MapsExploreActivity.restaurantsList.get(Integer.parseInt(name_and_id[1]));
                    Double distance = restaurants.getDistance();
                    String s_distance = distance<1000? String.format("%.0f metres away",distance) : String.format("%.0f km away",restaurants.getDistance()/1000);
                    mRow[2]= s_distance;

                    mRow[3] = getContext().getResources().getIdentifier(getRestaurantName(rec),
                            "drawable", getContext().getPackageName());
                    mRow[4] = ""+name_and_id[1];

                    searchResults.addRow(mRow);
                }
            }
        }
        return searchResults;
    }

    private String getRestaurantName(String suggestion){
        String suggestionWords[] = suggestion.split(" ");
        return suggestionWords[0].toLowerCase();
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String s, @Nullable String[] strings) {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String s, @Nullable String[] strings) {
        return 0;
    }
}

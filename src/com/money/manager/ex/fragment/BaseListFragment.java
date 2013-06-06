/*******************************************************************************
 * Copyright (C) 2012 The Android Money Manager Ex Project
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 ******************************************************************************/
package com.money.manager.ex.fragment;

import android.animation.LayoutTransition;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.widget.SearchViewCompat;
import android.support.v4.widget.SearchViewCompat.OnQueryTextListenerCompat;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.app.SherlockListFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.widget.SearchView;
import com.actionbarsherlock.widget.SearchView.OnQueryTextListener;
import com.money.manager.ex.R;
import com.money.manager.ex.core.Core;

public class BaseListFragment extends SherlockListFragment {
	// ID MENU
	private static final int MENU_ITEM_SEARCH = 1000;
	// saved instance
	private static final String KEY_SHOWN_TIPS_WILDCARD = "BaseListFragment:isShowTipsWildcard";
	// stato della visualizzazione menu
	private boolean mDisplayShowCustomEnabled = false;
	// flag che per la visualizzazione del menu'
	private boolean mShowMenuItemSearch = false;
	// flag for tips wildcard
	private boolean isShowTipsWildcard = false;
	
	@Override
	public void onCreateOptionsMenu(Menu menu, com.actionbarsherlock.view.MenuInflater inflater) {
		if (isShowMenuItemSearch()) {
	        // Place an action bar item for searching.
	        final MenuItem itemSearch = menu.add(0, MENU_ITEM_SEARCH, MENU_ITEM_SEARCH, R.string.search);
	        itemSearch.setIcon(new Core(getActivity()).resolveIdAttribute(R.attr.ic_action_search));
	        itemSearch.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
	        // uso il Compat per avere l'oggetto
	        View searchView = SearchViewCompat.newSearchView(getSherlockActivity().getSupportActionBar().getThemedContext());
	        if (searchView != null) {
	            SearchViewCompat.setOnQueryTextListener(searchView,
	                    new OnQueryTextListenerCompat() {
	                @Override
	                public boolean onQueryTextChange(String newText) {
	                    return BaseListFragment.this.onQueryTextChange(newText);
	                }
	            });
	            itemSearch.setActionView(searchView);
	        } else {
	        	SearchView actionSearchView = new SearchView(getSherlockActivity().getSupportActionBar().getThemedContext());
	        	actionSearchView.setOnQueryTextListener(new OnQueryTextListener() {
					
					@Override
					public boolean onQueryTextSubmit(String query) {
						return false;
					}
					
					@Override
					public boolean onQueryTextChange(String newText) {
						return BaseListFragment.this.onQueryTextChange(newText);
					}
				});
	        	itemSearch.setActionView(actionSearchView);
	        }
		}
	}
	
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch (item.getItemId()) {
    	case android.R.id.home:
    		// set result and exit
    		this.setResultAndFinish();
    		
    		break;
    	case MENU_ITEM_SEARCH:
    		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
    			onMenuItemSearchClick(item);
    		}
    		break;
    	}
    	return false;
    }
	
	protected boolean onQueryTextChange(String newText) {
		return true;
	}
	
	@Override
	public void onStart() {
		super.onStart();
		if (isShowMenuItemSearch() && !isShowTipsWildcard) {
			//show tooltip for wildcard
			TipsDialogFragment tipsDropbox = TipsDialogFragment.getInstance(getSherlockActivity().getApplicationContext(), "passtodropbox2");
			if (tipsDropbox != null) {
				tipsDropbox.setTips(getString(R.string.lookups_wildcard));
				//tipsDropbox.setCheckDontShowAgain(true);
				tipsDropbox.show(getSherlockActivity().getSupportFragmentManager(), "lookupswildcard");
				isShowTipsWildcard = true; // set shown
			}
		}
	}
	
    protected void onMenuItemSearchClick(MenuItem item) {
		View searchView = ((SherlockFragmentActivity)getActivity()).getSupportActionBar().getCustomView();
		final EditText edtSearch = (EditText)searchView.findViewById(R.id.editTextSearchView);
		InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
		// se in visualizzazione prendo l'edittext
		if (mDisplayShowCustomEnabled == false) {
			// rendo visibile l'edittext di ricerca
			edtSearch.setText("");
			edtSearch.requestFocus();
			// rendo visibile la keyboard
			imm.showSoftInput(edtSearch, 0);
			item.setActionView(searchView);
			// aggiorno lo stato
			mDisplayShowCustomEnabled = true;
		} else {
			// controllo se ho del testo lo pulisco
			if (TextUtils.isEmpty(edtSearch.getText().toString())) {
				// nascondo la keyboard
				imm.hideSoftInputFromWindow(edtSearch.getWindowToken(), 0);
				// tolgo la searchview
				item.setActionView(null);
				// aggiorno lo stato
				mDisplayShowCustomEnabled = false;
			} else {
				// annullo il testo
				edtSearch.setText(null);
				onQueryTextChange("");
			}
		}	
    }

	/**
	 * @return the mShowMenuItemSearch
	 */
	public boolean isShowMenuItemSearch() {
		return mShowMenuItemSearch;
	}

	/**
	 * @param mShowMenuItemSearch the mShowMenuItemSearch to set
	 */
	public void setShowMenuItemSearch(boolean mShowMenuItemSearch) {
		this.mShowMenuItemSearch = mShowMenuItemSearch;
	}
	
	/**
	 * metodo per l'implementazione del ritorno dei dati
	 */
	protected void setResult() {}
	
	public void setResultAndFinish() {
		//chiamo l'impostazione dei dati e chiudo l'activity
		this.setResult();
		//chiudo l'activity dove sono collegato
		getActivity().finish();
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		// set animation
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
			getListView().setLayoutTransition(new LayoutTransition());
		// saved instance
		if (savedInstanceState != null) {
			if (savedInstanceState.containsKey(KEY_SHOWN_TIPS_WILDCARD)) isShowTipsWildcard = savedInstanceState.getBoolean(KEY_SHOWN_TIPS_WILDCARD);
		}
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putBoolean(KEY_SHOWN_TIPS_WILDCARD, isShowTipsWildcard);
		super.onSaveInstanceState(outState);
	}
}
	

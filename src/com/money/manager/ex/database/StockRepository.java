package com.money.manager.ex.database;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.util.Log;

import com.money.manager.ex.Constants;
import com.money.manager.ex.domainmodel.Stock;

import org.apache.commons.lang3.ArrayUtils;

import java.math.BigDecimal;

/**
 * Data repository for Stock entities.
 * This is an experiment on how to replace the current dataset objects.
 *
 * Created by Alen on 5/09/2015.
 */
public class StockRepository
    extends RepositoryBase {

    public StockRepository(Context context) {
        super(context, "stock_v1", DatasetType.TABLE, "stock");

    }

    @Override
    public String[] getAllColumns() {
        String [] idColumn = new String[] {
                "STOCKID AS _id"
        };

        return ArrayUtils.addAll(idColumn, tableColumns());
    }

    public String[] tableColumns() {
        return new String[] {
                TableStock.STOCKID,
                TableStock.HELDAT,
                TableStock.PURCHASEDATE,
                TableStock.STOCKNAME,
                TableStock.SYMBOL,
                TableStock.CURRENTPRICE,
                TableStock.NUMSHARES,
                TableStock.VALUE
        };
    }

    public Stock load(int id) {
        if (id == Constants.NOT_SET) return null;

        Cursor cursor = mContext.getContentResolver().query(this.getUri(),
                null,
                TableStock.STOCKID + "=?",
                new String[] { Integer.toString(id) },
                null);
        if (cursor == null) return null;

        Stock stock = null;
        if (cursor.moveToNext()) {
            stock = new Stock();
            stock.loadFromCursor(cursor);
        }

        cursor.close();

        return stock;
    }

    public ContentValues loadContentValues(int id) {
        if (id == Constants.NOT_SET) return null;

        Cursor cursor = mContext.getContentResolver().query(this.getUri(),
                null,
                TableStock.STOCKID + "=?",
                new String[] { Integer.toString(id)},
                null);
        if (cursor == null) return null;
        if (!cursor.moveToNext()) return null;

        ContentValues stockValues = new ContentValues();

        String[] columns = tableColumns();
        for(String column : columns) {
            DatabaseUtils.cursorDoubleToContentValuesIfPresent(cursor, stockValues, column);
        }

        cursor.close();

        return stockValues;
    }

    public boolean loadFor(int accountId) {
        boolean result = false;

        String selection = TableAccountList.ACCOUNTID + "=?";
        Cursor cursor = mContext.getContentResolver().query(this.getUri(),
                null,
                selection,
                new String[] { Integer.toString(accountId) },
                null
        );
        if (cursor == null) return false;

        // check if cursor is valid
        if (cursor.moveToFirst()) {
            this.setValueFromCursor(cursor);

            result = true;
        }
        cursor.close();

        return result;
    }

    /**
     * Retrieves all record ids which refer the given symbol.
     * @return array of ids of records which contain the symbol.
     */
    public int[] findIdsBySymbol(String symbol) {
        int[] result = null;

        Cursor cursor = mContext.getContentResolver().query(this.getUri(),
                new String[]{ TableStock.STOCKID },
                TableStock.SYMBOL + "=?", new String[]{symbol},
                null);

        if (cursor != null) {
            int records = cursor.getCount();
            result = new int[records];

            for (int i = 0; i < records; i++) {
                cursor.moveToNext();
                result[i] = cursor.getInt(cursor.getColumnIndex(TableStock.STOCKID));
            }
            cursor.close();
        }

        return result;
    }

    public boolean update(int id, ContentValues values) {
        boolean result = false;

        int updateResult = mContext.getContentResolver().update(this.getUri(),
                values,
                TableStock.STOCKID + "=?",
                new String[]{Integer.toString(id)}
        );

        if (updateResult != 0) {
            result = true;
        } else {
            Log.w(this.getClass().getSimpleName(), "Price update failed for stock id:" + id);
        }

        return  result;
    }

    /**
     * Update price for all the records with this symbol.
     * @param symbol
     * @param price
     */
    public void updateCurrentPrice(String symbol, BigDecimal price) {
        int[] ids = findIdsBySymbol(symbol);

        // recalculate value

        for (int id : ids) {
            //updatePrice(id, price);

            ContentValues oldValues = loadContentValues(id);
            double numberOfSharesD = oldValues.getAsDouble(TableStock.NUMSHARES);
            BigDecimal numberOfShares = new BigDecimal(numberOfSharesD);
            BigDecimal value = numberOfShares.multiply(price);

            ContentValues newValues = new ContentValues();
            newValues.put(TableStock.CURRENTPRICE, price.doubleValue());
            newValues.put(TableStock.VALUE, value.doubleValue());

            update(id, newValues);
        }
    }

    public int insert(ContentValues values) {

        Uri insertUri = mContext.getContentResolver().insert(this.getUri(),
                values);
        long id = ContentUris.parseId(insertUri);

        return (int) id;
    }
}

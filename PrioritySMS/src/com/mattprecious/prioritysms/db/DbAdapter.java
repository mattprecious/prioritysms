
package com.mattprecious.prioritysms.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;

import com.google.common.collect.Lists;
import com.mattprecious.prioritysms.model.BaseProfile;
import com.mattprecious.prioritysms.model.PhoneProfile;
import com.mattprecious.prioritysms.model.SmsProfile;

import java.util.List;

public class DbAdapter {
    // TODO: this is so dirty... the query is unreadable
    private static final String CONCAT_SEPARATOR = " | ";
    private static final String KEYWORDS_ALIAS = "keywords";
    private static final String CONTACTS_ALIAS = "contact_lookups";
    private static final String PROFILES_QUERY = String.format("" +
            "SELECT *, " +
            "group_concat(%5$s.%9$s, '%13$s') as %10$s, " +
            "group_concat(%7$s.%11$s, '%13$s') as %12$s " +
            "FROM %1$s " +
            "LEFT OUTER JOIN %3$s ON %1$s.%2$s = %3$s.%4$s " +
            "LEFT OUTER JOIN %5$s ON %1$s.%2$s = %5$s.%6$s " +
            "LEFT OUTER JOIN %7$s ON %1$s.%2$s = %7$s.%8$s " +
            "GROUP BY %1$s.%2$s " +
            "ORDER BY %1$s.%14$s",
            DbHelper.PROFILES_TABLE_NAME,
            DbHelper.PROFILES_KEY_ID,
            DbHelper.ACTIONS_TABLE_NAME,
            DbHelper.ACTIONS_KEY_PROFILE_ID,
            DbHelper.KEYWORDS_TABLE_NAME,
            DbHelper.KEYWORDS_KEY_PROFILE_ID,
            DbHelper.CONTACTS_TABLE_NAME,
            DbHelper.CONTACTS_KEY_PROFILE_ID,
            DbHelper.KEYWORDS_KEY_KEYWORD,
            KEYWORDS_ALIAS,
            DbHelper.CONTACTS_KEY_CONTACT_LOOKUP,
            CONTACTS_ALIAS,
            CONCAT_SEPARATOR,
            DbHelper.PROFILES_KEY_NAME);

    private DbHelper dbHelper;
    private SQLiteDatabase db;

    public DbAdapter(Context context) {
        dbHelper = new DbHelper(context);
    }

    public void openReadable() throws SQLException {
        db = dbHelper.getReadableDatabase();
    }

    public void openWritable() throws SQLException {
        db = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public List<BaseProfile> getProfiles() {
        List<BaseProfile> profiles = Lists.newArrayList();

        Cursor c = db.rawQuery(PROFILES_QUERY, null);

        c.moveToFirst();
        while (!c.isAfterLast()) {
            profiles.add(cursorToProfile(c));
            c.moveToNext();
        }

        return profiles;
    }

    public boolean insertProfile(BaseProfile profile) {
        db.beginTransaction();
        try {
            ContentValues profileValues = profileToProfileValues(profile);
            long rowId = db.insert(DbHelper.PROFILES_TABLE_NAME, null, profileValues);
            if (rowId < 0) {
                return false;
            }

            profile.setId(rowId);

            ContentValues actionValues = profileToActionValues(profile);
            if (db.insert(DbHelper.ACTIONS_TABLE_NAME, null, actionValues) < 0) {
                return false;
            }

            List<ContentValues> contactValues = profileToContactValues(profile);
            for (ContentValues values : contactValues) {
                if (db.insert(DbHelper.CONTACTS_TABLE_NAME, null, values) < 0) {
                    return false;
                }
            }

            List<ContentValues> keywordValues = profileToKeywordValues(profile);
            for (ContentValues values : keywordValues) {
                if (db.insert(DbHelper.KEYWORDS_TABLE_NAME, null, values) < 0) {
                    return false;
                }
            }

            db.setTransactionSuccessful();
            return true;
        } catch (Exception e) {

        } finally {
            db.endTransaction();
        }

        return false;
    }

    public boolean updateProfile(BaseProfile profile) {
        ContentValues profileValues = profileToProfileValues(profile);
        ContentValues actionValues = profileToActionValues(profile);
        List<ContentValues> contactValues = profileToContactValues(profile);
        List<ContentValues> keywordValues = profileToKeywordValues(profile);

        final String profilesWhere = String.format("%s=?", DbHelper.PROFILES_KEY_ID);
        final String actionsWhere = String.format("%s=?", DbHelper.ACTIONS_KEY_PROFILE_ID);
        final String contactsWhere = String.format("%s=?", DbHelper.CONTACTS_KEY_PROFILE_ID);
        final String keywordsWhere = String.format("%s=?", DbHelper.KEYWORDS_KEY_PROFILE_ID);

        final String[] whereArgs = {
                String.valueOf(profile.getId())
        };

        db.beginTransaction();
        try {
            if (db.update(DbHelper.PROFILES_TABLE_NAME, profileValues, profilesWhere, whereArgs) < 0) {
                return false;
            }

            if (db.update(DbHelper.ACTIONS_TABLE_NAME, actionValues, actionsWhere, whereArgs) < 0) {
                return false;
            }

            db.delete(DbHelper.CONTACTS_TABLE_NAME, contactsWhere, whereArgs);
            for (ContentValues values : contactValues) {
                if (db.insert(DbHelper.CONTACTS_TABLE_NAME, null, values) < 0) {
                    return false;
                }
            }

            db.delete(DbHelper.KEYWORDS_TABLE_NAME, keywordsWhere, whereArgs);
            for (ContentValues values : keywordValues) {
                if (db.insert(DbHelper.KEYWORDS_TABLE_NAME, null, values) < 0) {
                    return false;
                }
            }

            db.setTransactionSuccessful();
            return true;
        } catch (Exception e) {

        } finally {
            db.endTransaction();
        }

        return false;
    }

    public boolean deleteProfile(BaseProfile profile) {
        String selection = String.format("%s=?", DbHelper.PROFILES_KEY_ID);
        String[] selectionArgs = {
                String.valueOf(profile.getId())
        };

        int result = db.delete(DbHelper.PROFILES_TABLE_NAME, selection, selectionArgs);

        return result > 0;
    }

    private BaseProfile cursorToProfile(Cursor c) {
        BaseProfile profile;

        // if SMS profile
        if (DbHelper.TYPE_ENUM_SMS.equals(c.getString(c
                .getColumnIndex(DbHelper.PROFILES_KEY_TYPE)))) {
            profile = new SmsProfile();

            SmsProfile smsProfile = (SmsProfile) profile;
            String[] keywords = getString(c, KEYWORDS_ALIAS).split(CONCAT_SEPARATOR);
            for (String keyword : keywords) {
                smsProfile.addKeyword(keyword);
            }
        } else {
            profile = new PhoneProfile();
        }

        profile.setId(getLong(c, DbHelper.PROFILES_KEY_ID));
        profile.setName(getString(c, DbHelper.PROFILES_KEY_NAME));
        profile.setEnabled(getBoolean(c, DbHelper.PROFILES_KEY_ENABLED));

        profile.setRingtone(getUri(c, DbHelper.ACTIONS_KEY_RINGTONE));
        profile.setVolume(getInt(c, DbHelper.ACTIONS_KEY_VOLUME));
        profile.setOverrideSilent(getBoolean(c, DbHelper.ACTIONS_KEY_OVERRIDE_SILENT));
        profile.setVibrate(getBoolean(c, DbHelper.ACTIONS_KEY_VIBRATE));

        String[] lookups = getString(c, CONTACTS_ALIAS).split(CONCAT_SEPARATOR);
        for (String lookup : lookups) {
            profile.addContact(lookup);
        }

        return profile;
    }

    private static ContentValues profileToProfileValues(BaseProfile profile) {
        ContentValues values = new ContentValues();

        if (profile.getId() >= 0) {
            values.put(DbHelper.PROFILES_KEY_ID, profile.getId());
        }

        values.put(DbHelper.PROFILES_KEY_NAME, profile.getName());
        values.put(DbHelper.PROFILES_KEY_TYPE,
                (profile instanceof SmsProfile) ? DbHelper.TYPE_ENUM_SMS : DbHelper.TYPE_ENUM_PHONE);
        values.put(DbHelper.PROFILES_KEY_ENABLED, profile.isEnabled());

        return values;
    }

    private static ContentValues profileToActionValues(BaseProfile profile) {
        ContentValues values = new ContentValues();
        values.put(DbHelper.ACTIONS_KEY_PROFILE_ID, profile.getId());

        Uri ringtone = profile.getRingtone();
        if (ringtone == null) {
            values.putNull(DbHelper.ACTIONS_KEY_RINGTONE);
        } else {
            values.put(DbHelper.ACTIONS_KEY_RINGTONE, ringtone.toString());
        }

        values.put(DbHelper.ACTIONS_KEY_VOLUME, profile.getVolume());
        values.put(DbHelper.ACTIONS_KEY_OVERRIDE_SILENT, profile.isOverrideSilent());
        values.put(DbHelper.ACTIONS_KEY_VIBRATE, profile.isVibrate());

        return values;
    }

    private static List<ContentValues> profileToContactValues(BaseProfile profile) {
        List<ContentValues> list = Lists.newArrayList();

        for (String lookup : profile.getContacts()) {
            ContentValues values = new ContentValues();
            values.put(DbHelper.CONTACTS_KEY_PROFILE_ID, profile.getId());
            values.put(DbHelper.CONTACTS_KEY_CONTACT_LOOKUP, lookup);

            list.add(values);
        }

        return list;
    }

    private static List<ContentValues> profileToKeywordValues(BaseProfile profile) {
        List<ContentValues> list = Lists.newArrayList();

        if (profile instanceof SmsProfile) {
            SmsProfile smsProfile = (SmsProfile) profile;
            for (String keyword : smsProfile.getKeywords()) {
                ContentValues values = new ContentValues();
                values.put(DbHelper.KEYWORDS_KEY_PROFILE_ID, smsProfile.getId());
                values.put(DbHelper.KEYWORDS_KEY_KEYWORD, keyword);

                list.add(values);
            }
        }

        return list;
    }

    private static int getInt(Cursor c, String columnName) {
        return c.getInt(c.getColumnIndexOrThrow(columnName));
    }

    private static long getLong(Cursor c, String columnName) {
        return c.getLong(c.getColumnIndexOrThrow(columnName));
    }

    private static String getString(Cursor c, String columnName) {
        return c.getString(c.getColumnIndexOrThrow(columnName));
    }

    private static Uri getUri(Cursor c, String columnName) {
        String path = getString(c, columnName);
        return (path != null) ? Uri.parse(path) : null;
    }

    private static boolean getBoolean(Cursor c, String columnName) {
        return c.getInt(c.getColumnIndexOrThrow(columnName)) > 0;
    }

    private static class DbHelper extends SQLiteOpenHelper {

        private static final int DATABASE_VERSION = 1;
        private static final String DATABASE_NAME = "profiles.db";

        private static final String PRAGMA = "PRAGMA foreign_keys=ON;";

        private static final String PROFILES_TABLE_NAME = "profiles";
        private static final String PROFILES_KEY_ID = "_id";
        private static final String PROFILES_KEY_NAME = "name";
        private static final String PROFILES_KEY_TYPE = "type";
        private static final String PROFILES_KEY_ENABLED = "enabled";

        private static final String TYPE_TABLE_NAME = "profile_type";
        private static final String TYPE_KEY_TYPE = "type";
        private static final String TYPE_KEY_NUM = "num";

        private static final String TYPE_ENUM_SMS = "sms";
        private static final String TYPE_ENUM_PHONE = "phone";

        private static final String CONTACTS_TABLE_NAME = "profile_contacts";
        private static final String CONTACTS_KEY_PROFILE_ID = "profile_id";
        private static final String CONTACTS_KEY_CONTACT_LOOKUP = "contact_lookup";

        private static final String KEYWORDS_TABLE_NAME = "profile_keywords";
        private static final String KEYWORDS_KEY_PROFILE_ID = "profile_id";
        private static final String KEYWORDS_KEY_KEYWORD = "keyword";

        private static final String ACTIONS_TABLE_NAME = "profile_actions";
        private static final String ACTIONS_KEY_PROFILE_ID = "profile_id";
        private static final String ACTIONS_KEY_RINGTONE = "ringtone";
        private static final String ACTIONS_KEY_OVERRIDE_SILENT = "override_silent";
        private static final String ACTIONS_KEY_VIBRATE = "vibrate";
        private static final String ACTIONS_KEY_VOLUME = "volume";

        private static final String PROFILES_TABLE_CREATE = String.format("" +
                "CREATE TABLE %s (" +
                "%s INTEGER PRIMARY KEY AUTOINCREMENT, " + // _id
                "%s TEXT NOT NULL, " + // name
                "%s TEXT NOT NULL REFERENCES %s(%s) ON UPDATE CASCADE, " + // type
                "%s INTEGER NOT NULL);", // enabled
                PROFILES_TABLE_NAME,
                PROFILES_KEY_ID,
                PROFILES_KEY_NAME,
                PROFILES_KEY_TYPE, TYPE_TABLE_NAME, TYPE_KEY_TYPE,
                PROFILES_KEY_ENABLED);

        private static final String TYPE_TABLE_CREATE = String.format("" +
                "CREATE TABLE %s (" +
                "%s TEXT PRIMARY KEY NOT NULL, " + // type
                "%s INTEGER NOT NULL);", // num
                TYPE_TABLE_NAME,
                TYPE_KEY_TYPE,
                TYPE_KEY_NUM);

        private static final String CONTACTS_TABLE_CREATE = String.format("" +
                "CREATE TABLE %s (" +
                "%s INTEGER KEY NOT NULL REFERENCES %s(%s) " + // profile_id
                "ON UPDATE CASCADE ON DELETE CASCADE, " +
                "%s INTEGER NOT NULL);", // contact_lookup
                CONTACTS_TABLE_NAME,
                CONTACTS_KEY_PROFILE_ID, PROFILES_TABLE_NAME, PROFILES_KEY_ID,
                CONTACTS_KEY_CONTACT_LOOKUP);

        private static final String KEYWORDS_TABLE_CREATE = String.format("" +
                "CREATE TABLE %s (" +
                "%s INTEGER KEY NOT NULL REFERENCES %s(%s) " + // profile_id
                "ON UPDATE CASCADE ON DELETE CASCADE, " +
                "%s TEXT NOT NULL);", // keyword
                KEYWORDS_TABLE_NAME,
                KEYWORDS_KEY_PROFILE_ID, PROFILES_TABLE_NAME, PROFILES_KEY_ID,
                KEYWORDS_KEY_KEYWORD);

        private static final String ACTIONS_TABLE_CREATE = String.format("" +
                "CREATE TABLE %s (" +
                "%s INTEGER KEY NOT NULL REFERENCES %s(%s) " + // profile_id
                "ON UPDATE CASCADE ON DELETE CASCADE, " +
                "%s TEXT, " + // ringtone
                "%s INTEGER NOT NULL, " + // override_silent
                "%s INTEGER NOT NULL, " + // vibrate
                "%s INTEGER NOT NULL);", // volume
                ACTIONS_TABLE_NAME,
                ACTIONS_KEY_PROFILE_ID, PROFILES_TABLE_NAME, PROFILES_KEY_ID,
                ACTIONS_KEY_RINGTONE,
                ACTIONS_KEY_OVERRIDE_SILENT,
                ACTIONS_KEY_VIBRATE,
                ACTIONS_KEY_VOLUME);

        private static final String TYPE_TABLE_POPULATE = String.format("" +
                "INSERT INTO %s(%s, %s) VALUES " +
                "('%s', 1)," + // sms
                "('%s', 2);", // phone
                TYPE_TABLE_NAME, TYPE_KEY_TYPE, TYPE_KEY_NUM,
                TYPE_ENUM_SMS,
                TYPE_ENUM_PHONE);

        public DbHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(PRAGMA);

            db.execSQL(PROFILES_TABLE_CREATE);
            db.execSQL(TYPE_TABLE_CREATE);
            db.execSQL(CONTACTS_TABLE_CREATE);
            db.execSQL(KEYWORDS_TABLE_CREATE);
            db.execSQL(ACTIONS_TABLE_CREATE);

            db.execSQL(TYPE_TABLE_POPULATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            switch (oldVersion) {

            }
        }

        @Override
        public synchronized SQLiteDatabase getReadableDatabase() {
            SQLiteDatabase db = super.getReadableDatabase();
            db.execSQL(PRAGMA);
            return db;
        }

        @Override
        public synchronized SQLiteDatabase getWritableDatabase() {
            SQLiteDatabase db = super.getWritableDatabase();
            db.execSQL(PRAGMA);
            return db;
        }
    }
}


package com.mattprecious.prioritysms.db;

import com.google.common.collect.Lists;

import com.mattprecious.prioritysms.model.BaseProfile;
import com.mattprecious.prioritysms.model.LogicMethod;
import com.mattprecious.prioritysms.model.PhoneProfile;
import com.mattprecious.prioritysms.model.SmsProfile;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;

import java.util.List;

public class DbAdapter {
    private static final String TAG = DbAdapter.class.getSimpleName();

    private static final String ASC = "ASC";
    private static final String DESC = "DESC";

    private static final String ORDER_FORMAT = "%s %s";

    private static final String JOIN_QUERY = String.format(
            "%1$s LEFT OUTER JOIN %3$s ON (%1$s.%2$s = %3$s.%4$s)",
            DbHelper.PROFILES_TABLE_NAME,
            DbHelper.PROFILES_KEY_ID,
            DbHelper.SMS_PROFILES_TABLE_NAME,
            DbHelper.SMS_PROFILES_KEY_PROFILE_ID);

    private DbHelper mDbHelper;

    public DbAdapter(Context context) {
        mDbHelper = new DbHelper(context);
    }

    public static enum SortOrder {
        NAME_ASC (DbHelper.PROFILES_KEY_NAME, ASC),
        NAME_DESC (DbHelper.PROFILES_KEY_NAME, DESC);

        private final String mQueryString;

        SortOrder(String key, String order) {
            mQueryString = String.format(ORDER_FORMAT, key, order);
        }

        public String getQueryString() {
            return mQueryString;
        }
    }

    public List<BaseProfile> getProfiles() {
        return getProfiles(SortOrder.NAME_ASC);
    }

    public List<BaseProfile> getProfiles(SortOrder sortOrder) {
        List<BaseProfile> profiles = Lists.newArrayList();

        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        try {
            SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
            builder.setTables(JOIN_QUERY);

            Cursor c = builder.query(db, null, null, null, null, null, sortOrder.getQueryString());

            c.moveToFirst();
            while (!c.isAfterLast()) {
                profiles.add(cursorToProfile(c));
                c.moveToNext();
            }
        } finally {
            db.close();
        }

        return profiles;
    }

    public List<SmsProfile> getEnabledSmsProfiles() {
        List<SmsProfile> profiles = Lists.newArrayList();

        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        try {
            SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
            builder.setTables(JOIN_QUERY);

            String selection = String.format("%s = ?", DbHelper.PROFILES_KEY_TYPE);
            String[] selectionArgs = {DbHelper.TYPE_ENUM_SMS};

            Cursor c = builder.query(db, null, selection, selectionArgs, null, null,
                    DbHelper.PROFILES_KEY_NAME);

            c.moveToFirst();
            while (!c.isAfterLast()) {
                profiles.add((SmsProfile) cursorToProfile(c));
                c.moveToNext();
            }
        } finally {
            db.close();
        }

        return profiles;
    }

    public List<PhoneProfile> getEnabledPhoneProfiles() {
        List<PhoneProfile> profiles = Lists.newArrayList();

        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        try {
            SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
            builder.setTables(JOIN_QUERY);

            String selection = String.format("%s=?", DbHelper.PROFILES_KEY_TYPE);
            String[] selectionArgs = {DbHelper.TYPE_ENUM_PHONE};

            Cursor c = builder.query(db, null, selection, selectionArgs, null, null,
                    DbHelper.PROFILES_KEY_NAME);

            c.moveToFirst();
            while (!c.isAfterLast()) {
                profiles.add((PhoneProfile) cursorToProfile(c));
                c.moveToNext();
            }
        } finally {
            db.close();
        }

        return profiles;
    }

    public boolean insertProfile(BaseProfile profile) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            ContentValues profileValues = profileToProfileValues(profile);
            long rowId = db.insert(DbHelper.PROFILES_TABLE_NAME, null, profileValues);
            if (rowId < 0) {
                return false;
            }

            profile.setId((int) rowId);

            ContentValues smsProfileValues = profileToSmsProfileValues(profile);
            if (smsProfileValues.size() > 0) {
                if (db.insert(DbHelper.SMS_PROFILES_TABLE_NAME, null, smsProfileValues) < 0) {
                    return false;
                }
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
            Log.e(TAG, "failed to insert profile", e);
        } finally {
            db.endTransaction();
            db.close();
        }

        return false;
    }

    public boolean updateProfile(BaseProfile profile) {
        ContentValues profileValues = profileToProfileValues(profile);
        ContentValues smsProfileValues = profileToSmsProfileValues(profile);
        List<ContentValues> contactValues = profileToContactValues(profile);
        List<ContentValues> keywordValues = profileToKeywordValues(profile);

        final String profilesWhere = String.format("%s=?", DbHelper.PROFILES_KEY_ID);
        final String smsProfilesWhere = String.format("%s=?", DbHelper.SMS_PROFILES_KEY_PROFILE_ID);
        final String contactsWhere = String.format("%s=?", DbHelper.CONTACTS_KEY_PROFILE_ID);
        final String keywordsWhere = String.format("%s=?", DbHelper.KEYWORDS_KEY_PROFILE_ID);

        final String[] whereArgs = {
                String.valueOf(profile.getId())
        };

        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            if (db.update(DbHelper.PROFILES_TABLE_NAME, profileValues, profilesWhere, whereArgs)
                    < 0) {
                return false;
            }

            db.delete(DbHelper.CONTACTS_TABLE_NAME, contactsWhere, whereArgs);
            for (ContentValues values : contactValues) {
                if (db.insert(DbHelper.CONTACTS_TABLE_NAME, null, values) < 0) {
                    return false;
                }
            }

            if (profile instanceof SmsProfile) {
                if (db.update(DbHelper.SMS_PROFILES_TABLE_NAME, smsProfileValues, smsProfilesWhere,
                        whereArgs) < 0) {
                    return false;
                }

                db.delete(DbHelper.KEYWORDS_TABLE_NAME, keywordsWhere, whereArgs);
                for (ContentValues values : keywordValues) {
                    if (db.insert(DbHelper.KEYWORDS_TABLE_NAME, null, values) < 0) {
                        return false;
                    }
                }
            }

            db.setTransactionSuccessful();
            return true;
        } catch (Exception e) {
            Log.e(TAG, "failed to update profile", e);
        } finally {
            db.endTransaction();
            db.close();
        }

        return false;
    }

    public boolean deleteProfile(BaseProfile profile) {
        int result = 0;
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        try {
            String selection = String.format("%s=?", DbHelper.PROFILES_KEY_ID);
            String[] selectionArgs = {
                    String.valueOf(profile.getId())
            };

            result = db.delete(DbHelper.PROFILES_TABLE_NAME, selection, selectionArgs);

        } finally {
            db.close();
        }

        return result > 0;
    }

    private BaseProfile cursorToProfile(Cursor c) {
        BaseProfile profile;

        // if SMS profile
        if (DbHelper.TYPE_ENUM_SMS.equals(c.getString(c
                .getColumnIndex(DbHelper.PROFILES_KEY_TYPE)))) {
            profile = new SmsProfile();
        } else {
            profile = new PhoneProfile();
        }

        profile.setId(getInt(c, DbHelper.PROFILES_KEY_ID));
        profile.setName(getString(c, DbHelper.PROFILES_KEY_NAME));
        profile.setEnabled(getBoolean(c, DbHelper.PROFILES_KEY_ENABLED));
        profile.setRingtone(getUri(c, DbHelper.PROFILES_KEY_RINGTONE));
        profile.setVibrate(getBoolean(c, DbHelper.PROFILES_KEY_VIBRATE));

        fillContactLookups(profile);

        if (profile instanceof SmsProfile) {
            String keywordMethodString = getString(c, DbHelper.SMS_PROFILES_KEY_KEYWORDS_METHOD);

            LogicMethod keywordMethod = LogicMethod.ANY;
            if (DbHelper.METHOD_ENUM_ALL.equals(keywordMethodString)) {
                keywordMethod = LogicMethod.ALL;
            } else if (DbHelper.METHOD_ENUM_ONLY.equals(keywordMethodString)) {
                keywordMethod = LogicMethod.ONLY;
            }

            SmsProfile smsProfile = (SmsProfile) profile;
            smsProfile.setKeywordMethod(keywordMethod);

            fillKeywords(smsProfile);
        }

        return profile;
    }

    private void fillContactLookups(BaseProfile profile) {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        try {
            String selection = String.format("%s=?", DbHelper.CONTACTS_KEY_PROFILE_ID);
            String[] selectionArgs = {
                    String.valueOf(profile.getId())
            };

            Cursor c = db.query(DbHelper.CONTACTS_TABLE_NAME, null, selection, selectionArgs, null,
                    null, null);

            c.moveToFirst();
            while (!c.isAfterLast()) {
                profile.addContact(getString(c, DbHelper.CONTACTS_KEY_CONTACT_LOOKUP));
                c.moveToNext();
            }
        } finally {
            db.close();
        }
    }

    private void fillKeywords(SmsProfile profile) {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        try {
            String selection = String.format("%s=?", DbHelper.KEYWORDS_KEY_PROFILE_ID);
            String[] selectionArgs = {
                    String.valueOf(profile.getId())
            };

            Cursor c = db.query(DbHelper.KEYWORDS_TABLE_NAME, null, selection, selectionArgs, null,
                    null, null);

            c.moveToFirst();
            while (!c.isAfterLast()) {
                profile.addKeyword(getString(c, DbHelper.KEYWORDS_KEY_KEYWORD));
                c.moveToNext();
            }
        } finally {
            db.close();
        }
    }

    private static ContentValues profileToProfileValues(BaseProfile profile) {
        ContentValues values = new ContentValues();

        if (profile.getId() >= 0) {
            values.put(DbHelper.PROFILES_KEY_ID, profile.getId());
        }

        values.put(DbHelper.PROFILES_KEY_NAME, profile.getName());
        values.put(DbHelper.PROFILES_KEY_TYPE,
                (profile instanceof SmsProfile) ? DbHelper.TYPE_ENUM_SMS
                        : DbHelper.TYPE_ENUM_PHONE);
        values.put(DbHelper.PROFILES_KEY_ENABLED, profile.isEnabled());

        Uri ringtone = profile.getRingtone();
        if (ringtone == null) {
            values.putNull(DbHelper.PROFILES_KEY_RINGTONE);
        } else {
            values.put(DbHelper.PROFILES_KEY_RINGTONE, ringtone.toString());
        }

        values.put(DbHelper.PROFILES_KEY_VIBRATE, profile.isVibrate());

        return values;
    }

    private static ContentValues profileToSmsProfileValues(BaseProfile profile) {
        ContentValues values = new ContentValues();

        if (profile instanceof SmsProfile) {
            SmsProfile smsProfile = (SmsProfile) profile;

            if (profile.getId() >= 0) {
                values.put(DbHelper.SMS_PROFILES_KEY_PROFILE_ID, profile.getId());
            }

            String methodString;
            switch (smsProfile.getKeywordMethod()) {
                case ALL:
                    methodString = DbHelper.METHOD_ENUM_ALL;
                    break;
                case ONLY:
                    methodString = DbHelper.METHOD_ENUM_ONLY;
                    break;
                case ANY:
                default:
                    methodString = DbHelper.METHOD_ENUM_ANY;
                    break;
            }

            values.put(DbHelper.SMS_PROFILES_KEY_KEYWORDS_METHOD, methodString);
        }

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
        private static final String PROFILES_KEY_RINGTONE = "ringtone";
        private static final String PROFILES_KEY_VIBRATE = "vibrate";

        private static final String TYPE_TABLE_NAME = "profile_type";
        private static final String TYPE_KEY_TYPE = "type";
        private static final String TYPE_KEY_NUM = "num";
        private static final String TYPE_ENUM_SMS = "sms";
        private static final String TYPE_ENUM_PHONE = "phone";

        private static final String SMS_PROFILES_TABLE_NAME = "sms_profiles";
        private static final String SMS_PROFILES_KEY_PROFILE_ID = "profile_id";
        private static final String SMS_PROFILES_KEY_KEYWORDS_METHOD = "keywords_method";

        private static final String METHOD_TABLE_NAME = "logic_method";
        private static final String METHOD_KEY_METHOD = "method";
        private static final String METHOD_KEY_NUM = "num";
        private static final String METHOD_ENUM_ALL = "all";
        private static final String METHOD_ENUM_ANY = "any";
        private static final String METHOD_ENUM_ONLY = "only";

        private static final String CONTACTS_TABLE_NAME = "profile_contacts";
        private static final String CONTACTS_KEY_PROFILE_ID = "profile_id";
        private static final String CONTACTS_KEY_CONTACT_LOOKUP = "contact_lookup";

        private static final String KEYWORDS_TABLE_NAME = "profile_keywords";
        private static final String KEYWORDS_KEY_PROFILE_ID = "profile_id";
        private static final String KEYWORDS_KEY_KEYWORD = "keyword";

        private static final String PROFILES_TABLE_CREATE = String.format("" +
                "CREATE TABLE %s (" +
                "%s INTEGER PRIMARY KEY AUTOINCREMENT, " + // _id
                "%s TEXT NOT NULL, " + // name
                "%s TEXT NOT NULL REFERENCES %s(%s) ON UPDATE CASCADE, " + // type
                "%s INTEGER NOT NULL, " + // enabled
                "%s TEXT, " + // ringtone
                "%s INTEGER NOT NULL);", // vibrate
                PROFILES_TABLE_NAME,
                PROFILES_KEY_ID,
                PROFILES_KEY_NAME,
                PROFILES_KEY_TYPE, TYPE_TABLE_NAME, TYPE_KEY_TYPE,
                PROFILES_KEY_ENABLED,
                PROFILES_KEY_RINGTONE,
                PROFILES_KEY_VIBRATE);

        private static final String TYPE_TABLE_CREATE = String.format("" +
                "CREATE TABLE %s (" +
                "%s TEXT PRIMARY KEY NOT NULL, " + // type
                "%s INTEGER NOT NULL);", // num
                TYPE_TABLE_NAME,
                TYPE_KEY_TYPE,
                TYPE_KEY_NUM);

        private static final String SMS_PROFILES_TABLE_CREATE = String.format("" +
                "CREATE TABLE %s (" +
                "%s INTEGER KEY NOT NULL REFERENCES %s(%s) " + // profile_id
                "ON UPDATE CASCADE ON DELETE CASCADE, " +
                "%s TEXT NOT NULL REFERENCES %s(%s) ON UPDATE CASCADE);", // method
                SMS_PROFILES_TABLE_NAME,
                SMS_PROFILES_KEY_PROFILE_ID, PROFILES_TABLE_NAME, PROFILES_KEY_ID,
                SMS_PROFILES_KEY_KEYWORDS_METHOD, METHOD_TABLE_NAME, METHOD_KEY_METHOD);

        private static final String METHOD_TABLE_CREATE = String.format("" +
                "CREATE TABLE %s (" +
                "%s TEXT PRIMARY KEY NOT NULL, " + // method
                "%s INTEGER NOT NULL);", // num
                METHOD_TABLE_NAME,
                METHOD_KEY_METHOD,
                METHOD_KEY_NUM);

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

        private static final String TYPE_TABLE_POPULATE = String.format("" +
                "INSERT INTO %s(%s, %s) VALUES " +
                "('%s', 1)," + // sms
                "('%s', 2);", // phone
                TYPE_TABLE_NAME, TYPE_KEY_TYPE, TYPE_KEY_NUM,
                TYPE_ENUM_SMS,
                TYPE_ENUM_PHONE);

        private static final String METHOD_TABLE_POPULATE = String.format("" +
                "INSERT INTO %s(%s, %s) VALUES " +
                "('%s', 1), " + // all
                "('%s', 2), " + // any
                "('%s', 3);", // only
                METHOD_TABLE_NAME, METHOD_KEY_METHOD, METHOD_KEY_NUM,
                METHOD_ENUM_ALL,
                METHOD_ENUM_ANY,
                METHOD_ENUM_ONLY);

        public DbHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(PRAGMA);

            db.execSQL(PROFILES_TABLE_CREATE);
            db.execSQL(TYPE_TABLE_CREATE);
            db.execSQL(SMS_PROFILES_TABLE_CREATE);
            db.execSQL(METHOD_TABLE_CREATE);
            db.execSQL(CONTACTS_TABLE_CREATE);
            db.execSQL(KEYWORDS_TABLE_CREATE);

            db.execSQL(TYPE_TABLE_POPULATE);
            db.execSQL(METHOD_TABLE_POPULATE);
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

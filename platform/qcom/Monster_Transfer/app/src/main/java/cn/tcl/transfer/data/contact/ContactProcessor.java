/*Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.transfer.data.contact;

import android.accounts.Account;
import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.RawContactsEntity;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.android.vcard.VCardComposer;
import com.android.vcard.VCardConfig;
import com.android.vcard.VCardEntryCommitter;
import com.android.vcard.VCardEntryConstructor;
import com.android.vcard.VCardInterpreter;
import com.android.vcard.VCardParser;
import com.android.vcard.VCardParser_V21;
import com.android.vcard.VCardParser_V30;
import com.android.vcard.exception.VCardException;
import com.android.vcard.exception.VCardNestedException;
import com.android.vcard.exception.VCardNotSupportedException;
import com.android.vcard.exception.VCardVersionException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import cn.tcl.transfer.systemApp.ContactsSysApp;
import cn.tcl.transfer.util.Utils;


public class ContactProcessor {

    private final static String TAG = "ExportProcessor";
    private final Context mContext;
    private final ContentResolver mResolver;
    private boolean mCanceled = false;

    final static int VCARD_VERSION_AUTO_DETECT = 0;
    final static int VCARD_VERSION_V21 = 1;
    final static int VCARD_VERSION_V30 = 2;

    public ContactProcessor(Context context) {
        mContext = context;
        mResolver = context.getContentResolver();
    }

    /**
     * export contacts to a vcf file
     * @param dest - the path of vcf file
     */
    public void backupContacts(@NonNull String dest) {
        VCardComposer composer = null;
        Writer writer = null;
        boolean successful = false;
        try {
            if (TextUtils.isEmpty(dest)) {
                return;
            }

            final OutputStream outputStream;
            try {
                File file = new File(Utils.SYS_SQL_DATA_BACKUP_PATH);
                if(!file.exists()) {
                    file.mkdirs();
                }
                File backFile = new File(dest);
                if(!backFile.exists()) {
                    backFile.createNewFile();
                }
                final Uri uri = Uri.fromFile(backFile);
                outputStream = mResolver.openOutputStream(uri);
            } catch (FileNotFoundException e) {
                Log.w(TAG, "FileNotFoundException thrown", e);
                return;
            } catch (IOException e) {
                Log.e(TAG, "IOException thrown", e);
                return;
            }

            composer = new VCardComposer(mContext, VCardConfig.VCARD_TYPE_DEFAULT, true);

            writer = new BufferedWriter(new OutputStreamWriter(outputStream));
            final Uri contentUriForRawContactsEntity = RawContactsEntity.CONTENT_URI;
            // TODO: should provide better selection.
            if (!composer.init(Contacts.CONTENT_URI, new String[] {Contacts._ID},
                    null, null,
                    null, contentUriForRawContactsEntity)) {
                final String errorReason = composer.getErrorReason();
                Log.e(TAG, "initialization of vCard composer failed: " + errorReason);
                return;
            }

            final int total = composer.getCount();
            if (total == 0) {
                Log.e(TAG, "There is no exportable contact.");
                return;
            }

            int current = 1;  // 1-origin
            ContactsSysApp.localCount = 0;
            while (!composer.isAfterLast()) {
                if (mCanceled) {
                    Log.i(TAG, "Export request is cancelled during composing vCard");
                    return;
                }
                try {
                    writer.write(composer.createOneEntry());
                    ContactsSysApp.localCount++;
                } catch (IOException e) {
                    final String errorReason = composer.getErrorReason();
                    Log.e(TAG, "Failed to read a contact: " + errorReason);
                    return;
                }

                // vCard export is quite fast (compared to import), and frequent notifications
                // bother notification bar too much.
                if (current % 100 == 1) {
                    //doProgressNotification(uri, total, current);
                }
                current++;
            }
            Log.i(TAG, "Successfully finished exporting vCard: " + dest);

            successful = true;

        } finally {
            if (composer != null) {
                composer.terminate();
            }
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    Log.w(TAG, "IOException is thrown during close(). Ignored. " + e);
                }
            }
        }
    }

    /**
     * import contacts from a vcf file
     * @param source - the path of the vcf file
     * @param account - contacts will import a default account if 'null'
     */
    public void restoreContacts(@NonNull String source, Account account) {

        if (TextUtils.isEmpty(source)) {
            return;
        }
        File file = new File(source);
        if (!file.exists() || file.length() == 0) {
            return;
        }

        final int[] possibleVCardVersions = new int[] {
                    VCARD_VERSION_V21,
                    VCARD_VERSION_V30
        };

        final Uri uri = Uri.fromFile(new File(source));
        //mTotalCount += entryCount;

        final VCardEntryConstructor constructor =
                new VCardEntryConstructor(0, account, null);
        final VCardEntryCommitter committer = new VCardEntryCommitter(mResolver);
        constructor.addEntryHandler(committer);
        //constructor.addEntryHandler(this);

        InputStream is = null;
        boolean successful = false;
        try {
            if (uri != null) {
                Log.i(TAG, "start importing one vCard (Uri: " + uri + ")");
                is = mResolver.openInputStream(uri);
            } else {
                Log.e(TAG, "the uri of vCard is null");
            }

            if (is != null) {
                successful = readOneVCard(is, 0, null, constructor, possibleVCardVersions);
            } else {
                Log.e(TAG, "the InputStream is null");
            }
        } catch (IOException e) {
            successful = false;
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (Exception e) {
                    // ignore
                }
            }
        }

        if (successful) {
            Log.i(TAG, "import successfully");
        } else {
            Log.w(TAG, "Failed to read one vCard file: " + uri);
        }
    }

    private boolean readOneVCard(InputStream is, int vcardType, String charset,
                                 final VCardInterpreter interpreter,
                                 final int[] possibleVCardVersions) {
        boolean successful = false;
        final int length = possibleVCardVersions.length;
        VCardParser vCardParser = null;
        for (int i = 0; i < length; i++) {
            final int vcardVersion = possibleVCardVersions[i];
            try {
                if (i > 0 && (interpreter instanceof VCardEntryConstructor)) {
                    // Let the object clean up internal temporary objects,
                    ((VCardEntryConstructor) interpreter).clear();
                }

                // We need synchronized block here,
                // since we need to handle mCanceled and mVCardParser at once.
                // In the worst case, a user may call cancel() just before creating
                // mVCardParser.
                synchronized (this) {
                    vCardParser = (vcardVersion == VCARD_VERSION_V30 ?
                            new VCardParser_V30(vcardType) :
                            new VCardParser_V21(vcardType));
                    /*if (isCancelled()) {
                        Log.i(LOG_TAG, "ImportProcessor already recieves cancel request, so " +
                                "send cancel request to vCard parser too.");
                        vCardParser.cancel();
                    }*/
                }
                vCardParser.parse(is, interpreter);

                successful = true;
                break;
            } catch (IOException e) {
                Log.e(TAG, "IOException was emitted: " + e.getMessage());
            } catch (VCardNestedException e) {
                // This exception should not be thrown here. We should instead handle it
                // in the preprocessing session in ImportVCardActivity, as we don't try
                // to detect the type of given vCard here.
                //
                // TODO: Handle this case appropriately, which should mean we have to have
                // code trying to auto-detect the type of given vCard twice (both in
                // ImportVCardActivity and ImportVCardService).
                Log.e(TAG, "Nested Exception is found.");
            } catch (VCardNotSupportedException e) {
                Log.e(TAG, e.toString());
            } catch (VCardVersionException e) {
                if (i == length - 1) {
                    Log.e(TAG, "Appropriate version for this vCard is not found.");
                } else {
                    // We'll try the other (v30) version.
                }
            } catch (VCardException e) {
                Log.e(TAG, e.toString());
            } finally {
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException e) {
                    }
                }
            }
        }

        return successful;
    }
}

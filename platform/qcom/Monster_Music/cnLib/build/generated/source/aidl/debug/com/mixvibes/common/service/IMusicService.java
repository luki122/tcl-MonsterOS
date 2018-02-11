/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: /home/ximoon/document/git/Monster_Music/cnLib/src/main/aidl/com/mixvibes/common/service/IMusicService.aidl
 */
package com.mixvibes.common.service;
public interface IMusicService extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements com.mixvibes.common.service.IMusicService
{
private static final java.lang.String DESCRIPTOR = "com.mixvibes.common.service.IMusicService";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an com.mixvibes.common.service.IMusicService interface,
 * generating a proxy if needed.
 */
public static com.mixvibes.common.service.IMusicService asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof com.mixvibes.common.service.IMusicService))) {
return ((com.mixvibes.common.service.IMusicService)iin);
}
return new com.mixvibes.common.service.IMusicService.Stub.Proxy(obj);
}
@Override public android.os.IBinder asBinder()
{
return this;
}
@Override public boolean onTransact(int code, android.os.Parcel data, android.os.Parcel reply, int flags) throws android.os.RemoteException
{
switch (code)
{
case INTERFACE_TRANSACTION:
{
reply.writeString(DESCRIPTOR);
return true;
}
case TRANSACTION_isPlaying:
{
data.enforceInterface(DESCRIPTOR);
boolean _result = this.isPlaying();
reply.writeNoException();
reply.writeInt(((_result)?(1):(0)));
return true;
}
case TRANSACTION_pause:
{
data.enforceInterface(DESCRIPTOR);
this.pause();
reply.writeNoException();
return true;
}
case TRANSACTION_play:
{
data.enforceInterface(DESCRIPTOR);
this.play();
reply.writeNoException();
return true;
}
case TRANSACTION_prev:
{
data.enforceInterface(DESCRIPTOR);
this.prev();
reply.writeNoException();
return true;
}
case TRANSACTION_next:
{
data.enforceInterface(DESCRIPTOR);
this.next();
reply.writeNoException();
return true;
}
case TRANSACTION_duration:
{
data.enforceInterface(DESCRIPTOR);
long _result = this.duration();
reply.writeNoException();
reply.writeLong(_result);
return true;
}
case TRANSACTION_position:
{
data.enforceInterface(DESCRIPTOR);
long _result = this.position();
reply.writeNoException();
reply.writeLong(_result);
return true;
}
case TRANSACTION_seek:
{
data.enforceInterface(DESCRIPTOR);
long _arg0;
_arg0 = data.readLong();
this.seek(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_getTitleName:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _result = this.getTitleName();
reply.writeNoException();
reply.writeString(_result);
return true;
}
case TRANSACTION_getAlbumName:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _result = this.getAlbumName();
reply.writeNoException();
reply.writeString(_result);
return true;
}
case TRANSACTION_getArtistName:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _result = this.getArtistName();
reply.writeNoException();
reply.writeString(_result);
return true;
}
case TRANSACTION_getPath:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _result = this.getPath();
reply.writeNoException();
reply.writeString(_result);
return true;
}
case TRANSACTION_getAlbumCover:
{
data.enforceInterface(DESCRIPTOR);
android.graphics.Bitmap _result = this.getAlbumCover();
reply.writeNoException();
if ((_result!=null)) {
reply.writeInt(1);
_result.writeToParcel(reply, android.os.Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
}
else {
reply.writeInt(0);
}
return true;
}
case TRANSACTION_getIcon:
{
data.enforceInterface(DESCRIPTOR);
android.graphics.Bitmap _result = this.getIcon();
reply.writeNoException();
if ((_result!=null)) {
reply.writeInt(1);
_result.writeToParcel(reply, android.os.Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
}
else {
reply.writeInt(0);
}
return true;
}
case TRANSACTION_setRepeatMode:
{
data.enforceInterface(DESCRIPTOR);
int _arg0;
_arg0 = data.readInt();
this.setRepeatMode(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_getRepeatMode:
{
data.enforceInterface(DESCRIPTOR);
int _result = this.getRepeatMode();
reply.writeNoException();
reply.writeInt(_result);
return true;
}
case TRANSACTION_isInBackground:
{
data.enforceInterface(DESCRIPTOR);
boolean _result = this.isInBackground();
reply.writeNoException();
reply.writeInt(((_result)?(1):(0)));
return true;
}
case TRANSACTION_registerCallback:
{
data.enforceInterface(DESCRIPTOR);
com.mixvibes.common.service.IMusicServiceCallback _arg0;
_arg0 = com.mixvibes.common.service.IMusicServiceCallback.Stub.asInterface(data.readStrongBinder());
this.registerCallback(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_unregisterCallback:
{
data.enforceInterface(DESCRIPTOR);
com.mixvibes.common.service.IMusicServiceCallback _arg0;
_arg0 = com.mixvibes.common.service.IMusicServiceCallback.Stub.asInterface(data.readStrongBinder());
this.unregisterCallback(_arg0);
reply.writeNoException();
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements com.mixvibes.common.service.IMusicService
{
private android.os.IBinder mRemote;
Proxy(android.os.IBinder remote)
{
mRemote = remote;
}
@Override public android.os.IBinder asBinder()
{
return mRemote;
}
public java.lang.String getInterfaceDescriptor()
{
return DESCRIPTOR;
}
// boolean isHaveSong();			//Are there some songs on phone.

@Override public boolean isPlaying() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
boolean _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_isPlaying, _data, _reply, 0);
_reply.readException();
_result = (0!=_reply.readInt());
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
//playback status
// void open(String path);			// Plays song of specified path 
// void stop();					//stop playback

@Override public void pause() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_pause, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
//Pauses the song of playing.

@Override public void play() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_play, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
// Plays song(The step contains Mediaplayer setDataSource(),prepare(),start() status );

@Override public void prev() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_prev, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
// Plays previous song

@Override public void next() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_next, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
// Plays next song

@Override public long duration() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
long _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_duration, _data, _reply, 0);
_reply.readException();
_result = _reply.readLong();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
//Gets the duration of the file.

@Override public long position() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
long _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_position, _data, _reply, 0);
_reply.readException();
_result = _reply.readLong();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
//Gets the current playback position.

@Override public void seek(long pos) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeLong(pos);
mRemote.transact(Stub.TRANSACTION_seek, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
//Seeks to specified time position.
//Add by TCTNB,hongbin, 2015-05-06,PR994536 begin

@Override public java.lang.String getTitleName() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
java.lang.String _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getTitleName, _data, _reply, 0);
_reply.readException();
_result = _reply.readString();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
//Gets Title name of the playing song file.
//Add by TCTNB,hongbin, 2015-05-06,PR994536 end

@Override public java.lang.String getAlbumName() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
java.lang.String _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getAlbumName, _data, _reply, 0);
_reply.readException();
_result = _reply.readString();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
//Gets Album name of the playing song file.

@Override public java.lang.String getArtistName() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
java.lang.String _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getArtistName, _data, _reply, 0);
_reply.readException();
_result = _reply.readString();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
//Gets Artist Name of the playing song file.
//String getMIMEType();			//Gets MIME type of the playing song file.

@Override public java.lang.String getPath() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
java.lang.String _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getPath, _data, _reply, 0);
_reply.readException();
_result = _reply.readString();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
//Gets file path of the playing song file.
//int getAudioSessionId();		//Gets AudioSessionId of the MediaPlayer.

@Override public android.graphics.Bitmap getAlbumCover() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
android.graphics.Bitmap _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getAlbumCover, _data, _reply, 0);
_reply.readException();
if ((0!=_reply.readInt())) {
_result = android.graphics.Bitmap.CREATOR.createFromParcel(_reply);
}
else {
_result = null;
}
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
//Gets AlbumCover of the playing song file.

@Override public android.graphics.Bitmap getIcon() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
android.graphics.Bitmap _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getIcon, _data, _reply, 0);
_reply.readException();
if ((0!=_reply.readInt())) {
_result = android.graphics.Bitmap.CREATOR.createFromParcel(_reply);
}
else {
_result = null;
}
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
//Get album art for specified album.
//String getLyrics();				//Gets Lyrics of the playing song file.

@Override public void setRepeatMode(int repeatmode) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeInt(repeatmode);
mRemote.transact(Stub.TRANSACTION_setRepeatMode, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
//Sets repeat mode of play list.

@Override public int getRepeatMode() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getRepeatMode, _data, _reply, 0);
_reply.readException();
_result = _reply.readInt();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
//Gets repeat mode of play list.

@Override public boolean isInBackground() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
boolean _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_isInBackground, _data, _reply, 0);
_reply.readException();
_result = (0!=_reply.readInt());
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
//Get MvApplication.isInBackground(). //[BUGFIX]-Add by TCTNJ,liang.guo, 2015-7-3,PR1035607
/**
     * Often you want to allow a service to call back to its clients.
     * This shows how to do so, by registering a callback interface with
     * the service.
     */
@Override public void registerCallback(com.mixvibes.common.service.IMusicServiceCallback cb) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeStrongBinder((((cb!=null))?(cb.asBinder()):(null)));
mRemote.transact(Stub.TRANSACTION_registerCallback, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
/**
     * Remove a previously registered callback interface.
     */
@Override public void unregisterCallback(com.mixvibes.common.service.IMusicServiceCallback cb) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeStrongBinder((((cb!=null))?(cb.asBinder()):(null)));
mRemote.transact(Stub.TRANSACTION_unregisterCallback, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
}
static final int TRANSACTION_isPlaying = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
static final int TRANSACTION_pause = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
static final int TRANSACTION_play = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);
static final int TRANSACTION_prev = (android.os.IBinder.FIRST_CALL_TRANSACTION + 3);
static final int TRANSACTION_next = (android.os.IBinder.FIRST_CALL_TRANSACTION + 4);
static final int TRANSACTION_duration = (android.os.IBinder.FIRST_CALL_TRANSACTION + 5);
static final int TRANSACTION_position = (android.os.IBinder.FIRST_CALL_TRANSACTION + 6);
static final int TRANSACTION_seek = (android.os.IBinder.FIRST_CALL_TRANSACTION + 7);
static final int TRANSACTION_getTitleName = (android.os.IBinder.FIRST_CALL_TRANSACTION + 8);
static final int TRANSACTION_getAlbumName = (android.os.IBinder.FIRST_CALL_TRANSACTION + 9);
static final int TRANSACTION_getArtistName = (android.os.IBinder.FIRST_CALL_TRANSACTION + 10);
static final int TRANSACTION_getPath = (android.os.IBinder.FIRST_CALL_TRANSACTION + 11);
static final int TRANSACTION_getAlbumCover = (android.os.IBinder.FIRST_CALL_TRANSACTION + 12);
static final int TRANSACTION_getIcon = (android.os.IBinder.FIRST_CALL_TRANSACTION + 13);
static final int TRANSACTION_setRepeatMode = (android.os.IBinder.FIRST_CALL_TRANSACTION + 14);
static final int TRANSACTION_getRepeatMode = (android.os.IBinder.FIRST_CALL_TRANSACTION + 15);
static final int TRANSACTION_isInBackground = (android.os.IBinder.FIRST_CALL_TRANSACTION + 16);
static final int TRANSACTION_registerCallback = (android.os.IBinder.FIRST_CALL_TRANSACTION + 17);
static final int TRANSACTION_unregisterCallback = (android.os.IBinder.FIRST_CALL_TRANSACTION + 18);
}
// boolean isHaveSong();			//Are there some songs on phone.

public boolean isPlaying() throws android.os.RemoteException;
//playback status
// void open(String path);			// Plays song of specified path 
// void stop();					//stop playback

public void pause() throws android.os.RemoteException;
//Pauses the song of playing.

public void play() throws android.os.RemoteException;
// Plays song(The step contains Mediaplayer setDataSource(),prepare(),start() status );

public void prev() throws android.os.RemoteException;
// Plays previous song

public void next() throws android.os.RemoteException;
// Plays next song

public long duration() throws android.os.RemoteException;
//Gets the duration of the file.

public long position() throws android.os.RemoteException;
//Gets the current playback position.

public void seek(long pos) throws android.os.RemoteException;
//Seeks to specified time position.
//Add by TCTNB,hongbin, 2015-05-06,PR994536 begin

public java.lang.String getTitleName() throws android.os.RemoteException;
//Gets Title name of the playing song file.
//Add by TCTNB,hongbin, 2015-05-06,PR994536 end

public java.lang.String getAlbumName() throws android.os.RemoteException;
//Gets Album name of the playing song file.

public java.lang.String getArtistName() throws android.os.RemoteException;
//Gets Artist Name of the playing song file.
//String getMIMEType();			//Gets MIME type of the playing song file.

public java.lang.String getPath() throws android.os.RemoteException;
//Gets file path of the playing song file.
//int getAudioSessionId();		//Gets AudioSessionId of the MediaPlayer.

public android.graphics.Bitmap getAlbumCover() throws android.os.RemoteException;
//Gets AlbumCover of the playing song file.

public android.graphics.Bitmap getIcon() throws android.os.RemoteException;
//Get album art for specified album.
//String getLyrics();				//Gets Lyrics of the playing song file.

public void setRepeatMode(int repeatmode) throws android.os.RemoteException;
//Sets repeat mode of play list.

public int getRepeatMode() throws android.os.RemoteException;
//Gets repeat mode of play list.

public boolean isInBackground() throws android.os.RemoteException;
//Get MvApplication.isInBackground(). //[BUGFIX]-Add by TCTNJ,liang.guo, 2015-7-3,PR1035607
/**
     * Often you want to allow a service to call back to its clients.
     * This shows how to do so, by registering a callback interface with
     * the service.
     */
public void registerCallback(com.mixvibes.common.service.IMusicServiceCallback cb) throws android.os.RemoteException;
/**
     * Remove a previously registered callback interface.
     */
public void unregisterCallback(com.mixvibes.common.service.IMusicServiceCallback cb) throws android.os.RemoteException;
}

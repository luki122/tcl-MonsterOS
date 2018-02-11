/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: /home/ximoon/document/git/Monster_Music/app/src/main/aidl/cn/tcl/music/service/IMusicService.aidl
 */
package cn.tcl.music.service;
public interface IMusicService extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements cn.tcl.music.service.IMusicService
{
private static final java.lang.String DESCRIPTOR = "cn.tcl.music.service.IMusicService";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an cn.tcl.music.service.IMusicService interface,
 * generating a proxy if needed.
 */
public static cn.tcl.music.service.IMusicService asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof cn.tcl.music.service.IMusicService))) {
return ((cn.tcl.music.service.IMusicService)iin);
}
return new cn.tcl.music.service.IMusicService.Stub.Proxy(obj);
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
case TRANSACTION_stop:
{
data.enforceInterface(DESCRIPTOR);
this.stop();
reply.writeNoException();
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
int _result = this.duration();
reply.writeNoException();
reply.writeInt(_result);
return true;
}
case TRANSACTION_position:
{
data.enforceInterface(DESCRIPTOR);
int _result = this.position();
reply.writeNoException();
reply.writeInt(_result);
return true;
}
case TRANSACTION_seek:
{
data.enforceInterface(DESCRIPTOR);
int _arg0;
_arg0 = data.readInt();
this.seek(_arg0);
reply.writeNoException();
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
case TRANSACTION_getAlbumId:
{
data.enforceInterface(DESCRIPTOR);
long _result = this.getAlbumId();
reply.writeNoException();
reply.writeLong(_result);
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
case TRANSACTION_getArtistId:
{
data.enforceInterface(DESCRIPTOR);
long _result = this.getArtistId();
reply.writeNoException();
reply.writeLong(_result);
return true;
}
case TRANSACTION_playByMediaInfo:
{
data.enforceInterface(DESCRIPTOR);
cn.tcl.music.model.MediaInfo _arg0;
if ((0!=data.readInt())) {
_arg0 = cn.tcl.music.model.MediaInfo.CREATOR.createFromParcel(data);
}
else {
_arg0 = null;
}
this.playByMediaInfo(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_playByMediaID:
{
data.enforceInterface(DESCRIPTOR);
long _arg0;
_arg0 = data.readLong();
this.playByMediaID(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_playByMediaInfoIfNowPlay:
{
data.enforceInterface(DESCRIPTOR);
cn.tcl.music.model.MediaInfo _arg0;
if ((0!=data.readInt())) {
_arg0 = cn.tcl.music.model.MediaInfo.CREATOR.createFromParcel(data);
}
else {
_arg0 = null;
}
boolean _arg1;
_arg1 = (0!=data.readInt());
this.playByMediaInfoIfNowPlay(_arg0, _arg1);
reply.writeNoException();
return true;
}
case TRANSACTION_playByMediaIDIfNowPlay:
{
data.enforceInterface(DESCRIPTOR);
long _arg0;
_arg0 = data.readLong();
boolean _arg1;
_arg1 = (0!=data.readInt());
this.playByMediaIDIfNowPlay(_arg0, _arg1);
reply.writeNoException();
return true;
}
case TRANSACTION_getCurrentMediaID:
{
data.enforceInterface(DESCRIPTOR);
long _result = this.getCurrentMediaID();
reply.writeNoException();
reply.writeLong(_result);
return true;
}
case TRANSACTION_getIsPreparing:
{
data.enforceInterface(DESCRIPTOR);
boolean _result = this.getIsPreparing();
reply.writeNoException();
reply.writeInt(((_result)?(1):(0)));
return true;
}
case TRANSACTION_currentLoadingPerCent:
{
data.enforceInterface(DESCRIPTOR);
int _result = this.currentLoadingPerCent();
reply.writeNoException();
reply.writeInt(_result);
return true;
}
case TRANSACTION_getTitle:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _result = this.getTitle();
reply.writeNoException();
reply.writeString(_result);
return true;
}
case TRANSACTION_getArtWorkPath:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _result = this.getArtWorkPath();
reply.writeNoException();
reply.writeString(_result);
return true;
}
case TRANSACTION_getSongRemoteID:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _result = this.getSongRemoteID();
reply.writeNoException();
reply.writeString(_result);
return true;
}
case TRANSACTION_getMediaID:
{
data.enforceInterface(DESCRIPTOR);
long _result = this.getMediaID();
reply.writeNoException();
reply.writeLong(_result);
return true;
}
case TRANSACTION_setToCompletion:
{
data.enforceInterface(DESCRIPTOR);
boolean _arg0;
_arg0 = (0!=data.readInt());
this.setToCompletion(_arg0);
reply.writeNoException();
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements cn.tcl.music.service.IMusicService
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
@Override public void stop() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_stop, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
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
@Override public int duration() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_duration, _data, _reply, 0);
_reply.readException();
_result = _reply.readInt();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public int position() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_position, _data, _reply, 0);
_reply.readException();
_result = _reply.readInt();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public void seek(int pos) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeInt(pos);
mRemote.transact(Stub.TRANSACTION_seek, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
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
@Override public long getAlbumId() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
long _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getAlbumId, _data, _reply, 0);
_reply.readException();
_result = _reply.readLong();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
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
@Override public long getArtistId() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
long _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getArtistId, _data, _reply, 0);
_reply.readException();
_result = _reply.readLong();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public void playByMediaInfo(cn.tcl.music.model.MediaInfo info) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
if ((info!=null)) {
_data.writeInt(1);
info.writeToParcel(_data, 0);
}
else {
_data.writeInt(0);
}
mRemote.transact(Stub.TRANSACTION_playByMediaInfo, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public void playByMediaID(long media_id) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeLong(media_id);
mRemote.transact(Stub.TRANSACTION_playByMediaID, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public void playByMediaInfoIfNowPlay(cn.tcl.music.model.MediaInfo info, boolean playNow) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
if ((info!=null)) {
_data.writeInt(1);
info.writeToParcel(_data, 0);
}
else {
_data.writeInt(0);
}
_data.writeInt(((playNow)?(1):(0)));
mRemote.transact(Stub.TRANSACTION_playByMediaInfoIfNowPlay, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public void playByMediaIDIfNowPlay(long media_id, boolean playNow) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeLong(media_id);
_data.writeInt(((playNow)?(1):(0)));
mRemote.transact(Stub.TRANSACTION_playByMediaIDIfNowPlay, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public long getCurrentMediaID() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
long _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getCurrentMediaID, _data, _reply, 0);
_reply.readException();
_result = _reply.readLong();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public boolean getIsPreparing() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
boolean _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getIsPreparing, _data, _reply, 0);
_reply.readException();
_result = (0!=_reply.readInt());
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public int currentLoadingPerCent() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_currentLoadingPerCent, _data, _reply, 0);
_reply.readException();
_result = _reply.readInt();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public java.lang.String getTitle() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
java.lang.String _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getTitle, _data, _reply, 0);
_reply.readException();
_result = _reply.readString();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public java.lang.String getArtWorkPath() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
java.lang.String _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getArtWorkPath, _data, _reply, 0);
_reply.readException();
_result = _reply.readString();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public java.lang.String getSongRemoteID() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
java.lang.String _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getSongRemoteID, _data, _reply, 0);
_reply.readException();
_result = _reply.readString();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public long getMediaID() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
long _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getMediaID, _data, _reply, 0);
_reply.readException();
_result = _reply.readLong();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public void setToCompletion(boolean toCompletion) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeInt(((toCompletion)?(1):(0)));
mRemote.transact(Stub.TRANSACTION_setToCompletion, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
}
static final int TRANSACTION_isPlaying = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
static final int TRANSACTION_stop = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
static final int TRANSACTION_pause = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);
static final int TRANSACTION_play = (android.os.IBinder.FIRST_CALL_TRANSACTION + 3);
static final int TRANSACTION_prev = (android.os.IBinder.FIRST_CALL_TRANSACTION + 4);
static final int TRANSACTION_next = (android.os.IBinder.FIRST_CALL_TRANSACTION + 5);
static final int TRANSACTION_duration = (android.os.IBinder.FIRST_CALL_TRANSACTION + 6);
static final int TRANSACTION_position = (android.os.IBinder.FIRST_CALL_TRANSACTION + 7);
static final int TRANSACTION_seek = (android.os.IBinder.FIRST_CALL_TRANSACTION + 8);
static final int TRANSACTION_getAlbumName = (android.os.IBinder.FIRST_CALL_TRANSACTION + 9);
static final int TRANSACTION_getAlbumId = (android.os.IBinder.FIRST_CALL_TRANSACTION + 10);
static final int TRANSACTION_getArtistName = (android.os.IBinder.FIRST_CALL_TRANSACTION + 11);
static final int TRANSACTION_getArtistId = (android.os.IBinder.FIRST_CALL_TRANSACTION + 12);
static final int TRANSACTION_playByMediaInfo = (android.os.IBinder.FIRST_CALL_TRANSACTION + 13);
static final int TRANSACTION_playByMediaID = (android.os.IBinder.FIRST_CALL_TRANSACTION + 14);
static final int TRANSACTION_playByMediaInfoIfNowPlay = (android.os.IBinder.FIRST_CALL_TRANSACTION + 15);
static final int TRANSACTION_playByMediaIDIfNowPlay = (android.os.IBinder.FIRST_CALL_TRANSACTION + 16);
static final int TRANSACTION_getCurrentMediaID = (android.os.IBinder.FIRST_CALL_TRANSACTION + 17);
static final int TRANSACTION_getIsPreparing = (android.os.IBinder.FIRST_CALL_TRANSACTION + 18);
static final int TRANSACTION_currentLoadingPerCent = (android.os.IBinder.FIRST_CALL_TRANSACTION + 19);
static final int TRANSACTION_getTitle = (android.os.IBinder.FIRST_CALL_TRANSACTION + 20);
static final int TRANSACTION_getArtWorkPath = (android.os.IBinder.FIRST_CALL_TRANSACTION + 21);
static final int TRANSACTION_getSongRemoteID = (android.os.IBinder.FIRST_CALL_TRANSACTION + 22);
static final int TRANSACTION_getMediaID = (android.os.IBinder.FIRST_CALL_TRANSACTION + 23);
static final int TRANSACTION_setToCompletion = (android.os.IBinder.FIRST_CALL_TRANSACTION + 24);
}
public boolean isPlaying() throws android.os.RemoteException;
public void stop() throws android.os.RemoteException;
public void pause() throws android.os.RemoteException;
public void play() throws android.os.RemoteException;
public void prev() throws android.os.RemoteException;
public void next() throws android.os.RemoteException;
public int duration() throws android.os.RemoteException;
public int position() throws android.os.RemoteException;
public void seek(int pos) throws android.os.RemoteException;
public java.lang.String getAlbumName() throws android.os.RemoteException;
public long getAlbumId() throws android.os.RemoteException;
public java.lang.String getArtistName() throws android.os.RemoteException;
public long getArtistId() throws android.os.RemoteException;
public void playByMediaInfo(cn.tcl.music.model.MediaInfo info) throws android.os.RemoteException;
public void playByMediaID(long media_id) throws android.os.RemoteException;
public void playByMediaInfoIfNowPlay(cn.tcl.music.model.MediaInfo info, boolean playNow) throws android.os.RemoteException;
public void playByMediaIDIfNowPlay(long media_id, boolean playNow) throws android.os.RemoteException;
public long getCurrentMediaID() throws android.os.RemoteException;
public boolean getIsPreparing() throws android.os.RemoteException;
public int currentLoadingPerCent() throws android.os.RemoteException;
public java.lang.String getTitle() throws android.os.RemoteException;
public java.lang.String getArtWorkPath() throws android.os.RemoteException;
public java.lang.String getSongRemoteID() throws android.os.RemoteException;
public long getMediaID() throws android.os.RemoteException;
public void setToCompletion(boolean toCompletion) throws android.os.RemoteException;
}

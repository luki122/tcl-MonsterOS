/* Copyright Statement:
 *
 * This software/firmware and related documentation ("MediaTek Software") are
 * protected under relevant copyright laws. The information contained herein
 * is confidential and proprietary to MediaTek Inc. and/or its licensors.
 * Without the prior written permission of MediaTek inc. and/or its licensors,
 * any reproduction, modification, use or disclosure of MediaTek Software,
 * and information contained herein, in whole or in part, shall be strictly prohibited.
 *
 * MediaTek Inc. (C) 2010. All rights reserved.
 *
 * BY OPENING THIS FILE, RECEIVER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
 * THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE")
 * RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO RECEIVER ON
 * AN "AS-IS" BASIS ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL WARRANTIES,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR NONINFRINGEMENT.
 * NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY WHATSOEVER WITH RESPECT TO THE
 * SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY, INCORPORATED IN, OR
 * SUPPLIED WITH THE MEDIATEK SOFTWARE, AND RECEIVER AGREES TO LOOK ONLY TO SUCH
 * THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO. RECEIVER EXPRESSLY ACKNOWLEDGES
 * THAT IT IS RECEIVER'S SOLE RESPONSIBILITY TO OBTAIN FROM ANY THIRD PARTY ALL PROPER LICENSES
 * CONTAINED IN MEDIATEK SOFTWARE. MEDIATEK SHALL ALSO NOT BE RESPONSIBLE FOR ANY MEDIATEK
 * SOFTWARE RELEASES MADE TO RECEIVER'S SPECIFICATION OR TO CONFORM TO A PARTICULAR
 * STANDARD OR OPEN FORUM. RECEIVER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S ENTIRE AND
 * CUMULATIVE LIABILITY WITH RESPECT TO THE MEDIATEK SOFTWARE RELEASED HEREUNDER WILL BE,
 * AT MEDIATEK'S OPTION, TO REVISE OR REPLACE THE MEDIATEK SOFTWARE AT ISSUE,
 * OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE CHARGE PAID BY RECEIVER TO
 * MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE.
 *
 * The following software/firmware and/or related documentation ("MediaTek Software")
 * have been modified by MediaTek Inc. All revisions are subject to any receiver's
 * applicable license agreements with MediaTek Inc.
 */

/* //device/samples/SampleCode/src/com/android/samples/app/RemoteServiceInterface.java
**
** Copyright 2007, The Android Open Source Project
**
** Licensed under the Apache License, Version 2.0 (the "License"); 
** you may not use this file except in compliance with the License. 
** You may obtain a copy of the License at 
**
**     http://www.apache.org/licenses/LICENSE-2.0 
**
** Unless required by applicable law or agreed to in writing, software 
** distributed under the License is distributed on an "AS IS" BASIS, 
** WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
** See the License for the specific language governing permissions and 
** limitations under the License.
*/

package com.mixvibes.common.service;

import android.graphics.Bitmap;
import com.mixvibes.common.service.IMusicServiceCallback;


interface IMusicService
{
    // boolean isHaveSong();			//Are there some songs on phone.
    boolean isPlaying();			//playback status
    // void open(String path);			// Plays song of specified path 
    // void stop();					//stop playback
    void pause();					//Pauses the song of playing.
    void play();					// Plays song(The step contains Mediaplayer setDataSource(),prepare(),start() status );
    void prev();					// Plays previous song
    void next();					// Plays next song
    long duration();				//Gets the duration of the file.
    long position();				//Gets the current playback position.
    void seek(long pos);			//Seeks to specified time position.
    //Add by TCTNB,hongbin, 2015-05-06,PR994536 begin
    String getTitleName();			//Gets Title name of the playing song file.
    //Add by TCTNB,hongbin, 2015-05-06,PR994536 end
    String getAlbumName();			//Gets Album name of the playing song file.
    String getArtistName();			//Gets Artist Name of the playing song file.
    //String getMIMEType();			//Gets MIME type of the playing song file.
    String getPath();				//Gets file path of the playing song file.
    //int getAudioSessionId();		//Gets AudioSessionId of the MediaPlayer.
    Bitmap getAlbumCover(); 		//Gets AlbumCover of the playing song file.
    Bitmap getIcon();				//Get album art for specified album.
    //String getLyrics();				//Gets Lyrics of the playing song file.
    void setRepeatMode(int repeatmode);	//Sets repeat mode of play list.
    int getRepeatMode();			//Gets repeat mode of play list.
    boolean isInBackground();       //Get MvApplication.isInBackground(). //[BUGFIX]-Add by TCTNJ,liang.guo, 2015-7-3,PR1035607

    /**
     * Often you want to allow a service to call back to its clients.
     * This shows how to do so, by registering a callback interface with
     * the service.
     */
    void registerCallback(IMusicServiceCallback cb);
    
    /**
     * Remove a previously registered callback interface.
     */
    void unregisterCallback(IMusicServiceCallback cb);
    
    
    // Note : Requested to separate Service & App process :
    // API
    // mediaLoader().isBusy()
    // player().setPlayerParameter
    // analyser().getAnalysedBpm()
    // analyser().getAnalysedDownBeat()
    // analyser().cancelAnalysis
    // engine().changeAudioOutState
    // remoteMedia().ensureLogin  (if remoteProvider)
    // mixer().registerListener
    // player().registerListener(...) --> callback type for each parameter or each signature 
    //		--> the service create a new object that listen to native and callback the app through aidl 
    // remoteMedia().registerListener
    // remoteMedia().query
    // remoteMedia().anotherInstanceStarted
    // mixer().setMixerParameter
    // samplesManager(mSamplerIdx).loadBank
    // player().setDbParams
    // player().scratchEvent(
    // mediaLoader().getMedia
    // mediaLoader().loadMedia
    // recorder().registerListener
    // samplesManager(0).renamePad
    // player().syncPlayer
    // autoMixEngine().stop / start / nextTrack / nextTrack  alreday in aidl
    // player().setPlayerState
    // samplesManager.initializeRecordProcess / startRecord, stopRecord, releaseRecordProcess
    // samplesManager.deleteSampleAndUnload
    // samplesManager().getBankList()  getSampleList
    // samplesManager().unloadBank()
    // mediaLoader().cancelTasksForSampler()
    // player().getTrackDuration
    // fx().getFxList() selectFx activateFx setFxParam
    // fx().registerListener
	// mediaLoader().unLoadMedia
	// mediaLoader().isRunningAnalysis()
	
    // Application().registerAppBackgroundListener --> aidl callback
    // MixSession.modeListener --> aidl callback
    // MixSession.getCurrentMode()
    // mediaLoader().registerListener()  --> aidl callback
    
    // Booth_SessionManager.boothVersion
    // Booth_SessionManager.IsLocked()
    // Booth_SessionManager.askLockPwd(
    
    // autoMixEngine().registerAutomixStateListener  --> aidl callback
    // MediaQueue.getInstance(getActivity()).addQueueListener  --> aidl callback
    
    // MediaQueue, Playlist : use the ContentProvider to add track, etc...
}


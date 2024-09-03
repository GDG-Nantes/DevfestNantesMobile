//
//  DevFestNantesStore-extension.swift
//  DevFest Nantes
//
//  Created by Robin Caroff on 25/08/2024.
//  Copyright © 2024 orgName. All rights reserved.
//

import shared
import KMPNativeCoroutinesCore

extension DevFestNantesStore {
    func getPartners() -> NativeFlow<[PartnerCategory: [Partner_]], Error, KotlinUnit> {
        DevFestNantesStoreNativeKt.partners(self)
    }
    
    func getRooms() -> NativeFlow<Set<Room_>, Error, KotlinUnit> {
        DevFestNantesStoreNativeKt.rooms(self)
    }
    
    func getSessions() -> NativeFlow<[Session_], Error, KotlinUnit> {
        DevFestNantesStoreNativeKt.sessions(self)
    }
    
    func getSpeaker(id: String) -> NativeSuspend<Speaker_?, Error, KotlinUnit> {
        DevFestNantesStoreNativeKt.getSpeaker(self, id: id)
    }
    
    func getSpeakers() -> NativeFlow<[Speaker_], Error, KotlinUnit> {
        DevFestNantesStoreNativeKt.speakers(self)
    }
    
    func getSpeakerSessions(speakerId: String) -> NativeSuspend<[Session_], Error, KotlinUnit> {
        DevFestNantesStoreNativeKt.getSpeakerSessions(self, speakerId: speakerId)
    }
    
    func getVenue(language: ContentLanguage) -> NativeSuspend<Venue_, Error, KotlinUnit> {
        DevFestNantesStoreNativeKt.getVenue(self, language: language)
    }
}

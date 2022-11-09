////
////  MockDevFestNantesStore.swift
////  DevFest NantesTests
////
////  Created by Stéphane Rihet on 08/11/2022.
////  Copyright © 2022 orgName. All rights reserved.
////
//
//import Foundation
//@testable import DevFest_Nantes
//import shared
//
//class MockDevFestNantesStore: DevFestNantesStore {
//    
//    func getRoom(id: String, completionHandler: @escaping (Room?, Error?) -> Void) {}
//    
//    func getRooms(id: String) async throws -> Room? {
//        return Room(id: "1", name: "First Room")
//    }
//    
//    func getRoomNative(id: String) -> (@escaping (Room?, KotlinUnit) -> KotlinUnit, @escaping (Error, KotlinUnit) -> KotlinUnit) -> () -> KotlinUnit {
//        
//        let result = KotlinUnit()
//        return result
//        
//    }
//    
//    func getSession(id: String, completionHandler: @escaping (Session?, Error?) -> Void) {
//        
//    }
//    
//    func getSessions(id: String) async throws -> Session? {
//        return nil
//    }
//    
//    func getSessionNative(id: String) -> (@escaping (Session?, KotlinUnit) -> KotlinUnit, @escaping (Error, KotlinUnit) -> KotlinUnit) -> () -> KotlinUnit {
//    }
//    
//    func getSpeaker(id: String, completionHandler: @escaping (Speaker?, Error?) -> Void) {
//        
//    }
//    
//    func getSpeakers(id: String) async throws -> Speaker? {
//        return nil
//    }
//    
//    func getSpeakerNative(id: String) -> (@escaping (Speaker?, KotlinUnit) -> KotlinUnit, @escaping (Error, KotlinUnit) -> KotlinUnit) -> () -> KotlinUnit {
//    }
//    
//    func getVenue(language: ContentLanguage, completionHandler: @escaping (Venue?, Error?) -> Void) {
//        
//    }
//    
//    func getVenues(language: ContentLanguage) async throws -> Venue {
//        return Venue(address: "", description: "", floorPlanUrl: "", latitude: 154, longitude: 254, imageUrl: "", name: "")
//    }
//    
//    func getVenueNative(language: ContentLanguage) -> (@escaping (Venue, KotlinUnit) -> KotlinUnit, @escaping (Error, KotlinUnit) -> KotlinUnit) -> () -> KotlinUnit {
//        
//    }
//    
//    var agenda: Kotlinx_coroutines_coreFlow
//    
//    var agendaNative: (@escaping (Agenda, KotlinUnit) -> KotlinUnit, @escaping (Error?, KotlinUnit) -> KotlinUnit) -> () -> KotlinUnit
//    
//    var partners: Kotlinx_coroutines_coreFlow
//    
//    var partnersNative: (@escaping ([PartnerCategory : [Partner]], KotlinUnit) -> KotlinUnit, @escaping (Error?, KotlinUnit) -> KotlinUnit) -> () -> KotlinUnit
//    
//    var rooms: Kotlinx_coroutines_coreFlow
//    
//    var roomsNative: (@escaping (Set<Room>, KotlinUnit) -> KotlinUnit, @escaping (Error?, KotlinUnit) -> KotlinUnit) -> () -> KotlinUnit
//    
//    var sessions: Kotlinx_coroutines_coreFlow
//    
//    var sessionsNative: (@escaping ([Session], KotlinUnit) -> KotlinUnit, @escaping (Error?, KotlinUnit) -> KotlinUnit) -> () -> KotlinUnit
//    
//    var speakers: Kotlinx_coroutines_coreFlow
//    
//    var speakersNative: (@escaping ([Speaker], KotlinUnit) -> KotlinUnit, @escaping (Error?, KotlinUnit) -> KotlinUnit) -> () -> KotlinUnit
//}

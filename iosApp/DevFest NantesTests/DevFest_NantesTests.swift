//
//  DevFest_NantesTests.swift
//  DevFest NantesTests
//
//  Created by Stéphane Rihet on 07/11/2022.
//  Copyright © 2022 orgName. All rights reserved.
//

import XCTest
@testable import DevFest_Nantes
import shared
import Combine

final class DevFest_NantesTests: XCTestCase {
    var devFestViewModel: DevFestViewModel!
    
    var expectation: XCTestExpectation!


    // MARK: - Tests Life Cycle
    
    @MainActor override func setUp() {
        super.setUp()
        devFestViewModel = DevFestViewModel()
        expectation = XCTestExpectation(description: "Expectation")
    }

    @MainActor override func tearDown() {
        super.tearDown()
        devFestViewModel.favorites = []
        // Put teardown code here. This method is called after the invocation of each test method in the class.
    }
    
    @MainActor func testObserveSessions() async {

        _ = await devFestViewModel.observeSessions()
        self.expectation.fulfill()
        XCTAssertNotNil(devFestViewModel.$agendaContent)
        
        wait(for: [expectation], timeout: 0.1)

    }
    
    @MainActor func testObserveVenue() async {
        _ = await devFestViewModel.observeVenue()

        self.expectation.fulfill()
        XCTAssertNotNil(devFestViewModel.$venueContent)

        wait(for: [expectation], timeout: 0.1)

    }
    
    @MainActor func testObserveRooms() async  {
        _ = await devFestViewModel.observeRooms()

        self.expectation.fulfill()
        XCTAssertNotNil(devFestViewModel.$roomsContent)

        wait(for: [expectation], timeout: 0.1)

    }
    
    @MainActor func testObservePartners() async {
        _ = await devFestViewModel.observePartners()
        self.expectation.fulfill()
        XCTAssertNotNil(devFestViewModel.$partnersContent)
        
        wait(for: [expectation], timeout: 0.1)

    }
    
    @MainActor func testToggleFavorite() {
        let session: AgendaContent.Session = AgendaContent.Session(id: "SessionTest", abstract: "", category: nil, language: nil, complexity: nil, openFeedbackFormId: nil, speakers: [Speaker(id: "", bio: nil, company: nil, companyLogoUrl: nil, city: nil, name: "", photoUrl: "", socials: nil)], room: "", date: Date(), startDate: "", endDate: "", durationAndLanguage: "", title: "", sessionType: nil)
        //UserDefault is empty
        XCTAssert(devFestViewModel.favorites.isEmpty)
        //Add fake session to favorite
        devFestViewModel.toggleFavorite(ofSession: session)
        XCTAssertFalse(devFestViewModel.favorites.isEmpty)
        XCTAssertTrue(devFestViewModel.favorites.contains("SessionTest"))
        //Remove fake session to favorite
        devFestViewModel.toggleFavorite(ofSession: session)
        XCTAssertFalse(devFestViewModel.favorites.contains("SessionTest"))
        XCTAssert(devFestViewModel.favorites.isEmpty)
    }

}


//
//  NativeSuspendExtensions.swift
//  DevFest Nantes
//
//  Created by Stéphane RIHET on 25/09/2024.
//  Copyright © 2024 orgName. All rights reserved.
//

import Combine
import KMPNativeCoroutinesCombine
import KMPNativeCoroutinesCore
import shared

func nativeSuspendToPublisher<T>(_ nativeSuspend: @escaping NativeSuspend<T, Error, KotlinUnit>) -> AnyPublisher<T, Error> {
    createFuture(for: nativeSuspend).eraseToAnyPublisher()
}

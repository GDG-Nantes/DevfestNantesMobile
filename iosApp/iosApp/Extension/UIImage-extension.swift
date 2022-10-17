//
//  UIImage-extension.swift
//  DevFest Nantes
//
//  Created by Stéphane Rihet on 17/10/2022.
//  Copyright © 2022 orgName. All rights reserved.
//

import Foundation
import UIKit
import SwiftUI

import UIKit
    
extension UIImage {
  convenience init?(url: URL?) {
    guard let url = url else { return nil }
            
    do {
      self.init(data: try Data(contentsOf: url))
    } catch {
      print("Cannot load image from url: \(url) with error: \(error)")
      return nil
    }
  }
}

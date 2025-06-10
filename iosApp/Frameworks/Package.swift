// swift-tools-version:5.6
import PackageDescription

let package = Package(
    name: "Shared",
    platforms: [
        .iOS(.v18)
    ],
    products: [
        .library(
            name: "shared",
            targets: ["shared"]
        ),
    ],
    targets: [
        .binaryTarget(
            name: "shared",
            path: "shared.xcframework"
        ),
    ]
)

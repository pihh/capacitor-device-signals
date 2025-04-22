// swift-tools-version: 5.9
import PackageDescription

let package = Package(
    name: "CapacitorSignalTriangulation",
    platforms: [.iOS(.v14)],
    products: [
        .library(
            name: "CapacitorSignalTriangulation",
            targets: ["SignalTriangulationPlugin"])
    ],
    dependencies: [
        .package(url: "https://github.com/ionic-team/capacitor-swift-pm.git", from: "7.0.0")
    ],
    targets: [
        .target(
            name: "SignalTriangulationPlugin",
            dependencies: [
                .product(name: "Capacitor", package: "capacitor-swift-pm"),
                .product(name: "Cordova", package: "capacitor-swift-pm")
            ],
            path: "ios/Sources/SignalTriangulationPlugin"),
        .testTarget(
            name: "SignalTriangulationPluginTests",
            dependencies: ["SignalTriangulationPlugin"],
            path: "ios/Tests/SignalTriangulationPluginTests")
    ]
)
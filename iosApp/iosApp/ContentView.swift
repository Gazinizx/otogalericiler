import SwiftUI
import ComposeApp
import OSLog

struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        StartupDiagnostics.logger.notice("Creating Kotlin Compose root controller")
        let controller = MainViewControllerKt.MainViewController()
        StartupDiagnostics.logger.notice("Kotlin Compose root controller created")
        return controller
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

struct ContentView: View {
    var body: some View {
        ComposeView().ignoresSafeArea(.all)
    }
}

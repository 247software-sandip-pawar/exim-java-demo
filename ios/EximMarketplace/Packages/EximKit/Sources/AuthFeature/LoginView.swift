import SwiftUI
import DesignSystemKit

public struct LoginView: View {
    @State private var viewModel: AuthViewModel
    @State private var email = ""
    @State private var password = ""

    public init(viewModel: AuthViewModel) {
        _viewModel = State(initialValue: viewModel)
    }

    private var canSubmit: Bool {
        email.contains("@") && !password.isEmpty && !viewModel.isBusy
    }

    public var body: some View {
        ZStack {
            AmbientBackground()
            ScrollView {
                VStack(spacing: 20) {
                    VStack(spacing: 16) {
                        FormField("Email") {
                            TextField("you@company.com", text: $email)
                                .textContentType(.emailAddress)
                                .keyboardType(.emailAddress)
                                .textInputAutocapitalization(.never)
                                .autocorrectionDisabled()
                        }
                        FormField("Password") {
                            SecureField("Your password", text: $password)
                                .textContentType(.password)
                        }
                        if let message = viewModel.errorMessage {
                            Text(message)
                                .font(.footnote)
                                .foregroundStyle(.red)
                                .frame(maxWidth: .infinity, alignment: .leading)
                        }
                        Button {
                            Task { await viewModel.login(email: email, password: password) }
                        } label: {
                            if viewModel.isBusy {
                                ProgressView().tint(.white)
                            } else {
                                Text("Sign In")
                            }
                        }
                        .buttonStyle(.eximPrimary)
                        .disabled(!canSubmit)
                    }
                    .padding(Theme.padding)
                    .glassCard()
                }
                .padding(24)
                .frame(maxWidth: 480)
                .frame(maxWidth: .infinity)
                .padding(.top, 24)
            }
        }
        .navigationTitle("Sign In")
        .navigationBarTitleDisplayMode(.large)
    }
}

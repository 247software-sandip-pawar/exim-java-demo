import SwiftUI
import DomainKit
import DesignSystemKit

public struct RegisterView: View {
    @State private var viewModel: AuthViewModel
    @State private var companyName = ""
    @State private var companyType: CompanyType = .exporter
    @State private var country = ""
    @State private var adminName = ""
    @State private var email = ""
    @State private var password = ""

    public init(viewModel: AuthViewModel) {
        _viewModel = State(initialValue: viewModel)
    }

    private var canSubmit: Bool {
        !companyName.isEmpty && !adminName.isEmpty && email.contains("@")
            && password.count >= 8 && !viewModel.isBusy
    }

    public var body: some View {
        ZStack {
            AmbientBackground()
            ScrollView {
                VStack(spacing: 20) {
                    VStack(spacing: 16) {
                        FormField("Company Name") {
                            TextField("Acme Exports Pvt Ltd", text: $companyName)
                        }
                        FormField("Company Type") {
                            Picker("Company Type", selection: $companyType) {
                                ForEach(CompanyType.allCases) { type in
                                    Text(type.displayName).tag(type)
                                }
                            }
                            .pickerStyle(.menu)
                            .frame(maxWidth: .infinity, alignment: .leading)
                        }
                        FormField("Country (optional)") {
                            TextField("India", text: $country)
                        }
                        FormField("Your Name") {
                            TextField("Full name", text: $adminName)
                                .textContentType(.name)
                        }
                        FormField("Email") {
                            TextField("you@company.com", text: $email)
                                .textContentType(.emailAddress)
                                .keyboardType(.emailAddress)
                                .textInputAutocapitalization(.never)
                                .autocorrectionDisabled()
                        }
                        FormField("Password (min 8 characters)") {
                            SecureField("Create a password", text: $password)
                                .textContentType(.newPassword)
                        }
                        if let message = viewModel.errorMessage {
                            Text(message)
                                .font(.footnote)
                                .foregroundStyle(.red)
                                .frame(maxWidth: .infinity, alignment: .leading)
                        }
                        Button {
                            Task {
                                await viewModel.register(
                                    companyName: companyName,
                                    companyType: companyType,
                                    country: country,
                                    adminName: adminName,
                                    email: email,
                                    password: password
                                )
                            }
                        } label: {
                            if viewModel.isBusy {
                                ProgressView().tint(.white)
                            } else {
                                Text("Create Account")
                            }
                        }
                        .buttonStyle(.eximPrimary)
                        .disabled(!canSubmit)
                    }
                    .padding(Theme.padding)
                    .glassCard()

                    Text("This registers your company and makes you its admin.")
                        .font(.footnote)
                        .foregroundStyle(.secondary)
                }
                .padding(24)
                .frame(maxWidth: 480)
                .frame(maxWidth: .infinity)
            }
        }
        .navigationTitle("Create Account")
        .navigationBarTitleDisplayMode(.large)
    }
}

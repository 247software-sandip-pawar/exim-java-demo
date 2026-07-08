import SwiftUI
import CoreKit
import DomainKit
import DesignSystemKit

/// Start a conversation: subject + pick the partner company.
public struct NewConversationView: View {
    @Environment(\.dismiss) private var dismiss

    private let repository: MessagingRepository
    private let companyRepository: CompanyRepository
    private let myCompanyId: UUID
    private let onCreated: () -> Void

    @State private var subject = ""
    @State private var companies: [Company] = []
    @State private var selectedCompany: Company?
    @State private var isBusy = false
    @State private var errorMessage: String?

    public init(
        repository: MessagingRepository,
        companyRepository: CompanyRepository,
        myCompanyId: UUID,
        onCreated: @escaping () -> Void
    ) {
        self.repository = repository
        self.companyRepository = companyRepository
        self.myCompanyId = myCompanyId
        self.onCreated = onCreated
    }

    private var canSubmit: Bool { !subject.isEmpty && selectedCompany != nil && !isBusy }

    public var body: some View {
        NavigationStack {
            ZStack {
                AmbientBackground()
                ScrollView {
                    VStack(spacing: 16) {
                        FormField("Subject") {
                            TextField("e.g. Basmati rice pricing", text: $subject)
                        }
                        FormField("With Company") {
                            Picker("With Company", selection: $selectedCompany) {
                                Text("Select a company").tag(Company?.none)
                                ForEach(companies.filter { $0.id != myCompanyId }) { company in
                                    Text(company.name).tag(Optional(company))
                                }
                            }
                            .pickerStyle(.menu)
                            .frame(maxWidth: .infinity, alignment: .leading)
                        }
                        if let errorMessage {
                            Text(errorMessage)
                                .font(.footnote)
                                .foregroundStyle(.red)
                                .frame(maxWidth: .infinity, alignment: .leading)
                        }
                        Button {
                            Task { await create() }
                        } label: {
                            if isBusy { ProgressView().tint(.white) } else { Text("Start Conversation") }
                        }
                        .buttonStyle(.eximPrimary)
                        .disabled(!canSubmit)
                    }
                    .padding(Theme.padding)
                    .glassCard()
                    .padding(Theme.padding)
                    .frame(maxWidth: 560)
                    .frame(maxWidth: .infinity)
                }
            }
            .navigationTitle("New Conversation")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Cancel") { dismiss() }
                }
            }
            .task {
                do {
                    companies = try await companyRepository.companies(query: PageQuery(page: 0, size: 100, sort: "name,asc")).items
                } catch {
                    errorMessage = error.localizedDescription
                }
            }
        }
    }

    private func create() async {
        guard let selectedCompany else { return }
        isBusy = true
        errorMessage = nil
        defer { isBusy = false }
        do {
            _ = try await repository.startConversation(
                ConversationInput(subject: subject, rfqId: nil, participantCompanyIds: [myCompanyId, selectedCompany.id])
            )
            onCreated()
            dismiss()
        } catch {
            errorMessage = error.localizedDescription
        }
    }
}

import UIKit
import React

@objc(NativeJourneyViewManager)
class NativeJourneyViewManager: RCTViewManager {
  override func view() -> UIView! {
    return NativeJourneyView()
  }

  override static func requiresMainQueueSetup() -> Bool {
    true
  }
}

final class NativeJourneyView: UIView {
  private let stackView = UIStackView()

  override init(frame: CGRect) {
    super.init(frame: frame)
    backgroundColor = UIColor(red: 0.99, green: 0.97, blue: 0.91, alpha: 1.0)

    stackView.translatesAutoresizingMaskIntoConstraints = false

    stackView.axis = .vertical
    stackView.spacing = 16
    stackView.alignment = .fill

    addSubview(stackView)

    NSLayoutConstraint.activate([
      stackView.topAnchor.constraint(equalTo: topAnchor, constant: 16),
      stackView.leadingAnchor.constraint(equalTo: leadingAnchor, constant: 16),
      stackView.trailingAnchor.constraint(equalTo: trailingAnchor, constant: -16),
      stackView.bottomAnchor.constraint(equalTo: bottomAnchor, constant: -16),
    ])

    stackView.addArrangedSubview(tag(text: "Swift Native View", textColor: UIColor(red: 0.56, green: 0.24, blue: 0.09, alpha: 1.0), background: UIColor(red: 1.0, green: 0.94, blue: 0.87, alpha: 1.0)))
    stackView.addArrangedSubview(titleLabel(text: "A longer Swift native screen", size: 24, weight: .bold))
    stackView.addArrangedSubview(bodyLabel(text: "This section is rendered entirely with native iOS views so the workflow pauses for a full platform-native interlude before it returns to React Native."))

    let cards = [
      "Arrival board with a compact welcome note",
      "Agenda highlight with the next meeting milestone",
      "Room status card with a short readiness summary",
      "Speaker prep note with a few extra lines of copy",
      "Snack table reminder for the host team",
      "Exit checklist with a calmer wrap-up paragraph",
      "Final handoff card before the planner resumes",
    ]

    for (index, text) in cards.enumerated() {
      stackView.addArrangedSubview(card(
        index: String(format: "%02d", index + 1),
        title: "Native card \(String(format: "%02d", index + 1))",
        body: text
      ))
    }

  }

  required init?(coder: NSCoder) {
    fatalError("init(coder:) has not been implemented")
  }

  private func tag(text: String, textColor: UIColor, background: UIColor) -> UIView {
    let label = UILabel()
    label.text = text.uppercased()
    label.textColor = textColor
    label.backgroundColor = background
    label.font = .systemFont(ofSize: 11, weight: .bold)
    label.layer.cornerRadius = 10
    label.layer.masksToBounds = true
    label.textAlignment = .center
    label.numberOfLines = 1
    label.translatesAutoresizingMaskIntoConstraints = false
    let insets = UIEdgeInsets(top: 6, left: 12, bottom: 6, right: 12)
    let container = PaddingView(content: label, insets: insets)
    return container
  }

  private func titleLabel(text: String, size: CGFloat, weight: UIFont.Weight) -> UILabel {
    let label = UILabel()
    label.text = text
    label.textColor = UIColor(red: 0.09, green: 0.2, blue: 0.31, alpha: 1.0)
    label.font = .systemFont(ofSize: size, weight: weight)
    label.numberOfLines = 0
    return label
  }

  private func bodyLabel(text: String) -> UILabel {
    let label = UILabel()
    label.text = text
    label.textColor = UIColor(red: 0.30, green: 0.39, blue: 0.47, alpha: 1.0)
    label.font = .systemFont(ofSize: 15, weight: .regular)
    label.numberOfLines = 0
    label.setContentHuggingPriority(.defaultLow, for: .vertical)
    return label
  }

  private func card(index: String, title: String, body: String) -> UIView {
    let container = UIView()
    container.backgroundColor = .white
    container.layer.cornerRadius = 20
    container.layer.shadowColor = UIColor.black.cgColor
    container.layer.shadowOpacity = 0.05
    container.layer.shadowRadius = 12
    container.layer.shadowOffset = CGSize(width: 0, height: 6)

    let inner = UIStackView()
    inner.axis = .vertical
    inner.spacing = 10
    inner.translatesAutoresizingMaskIntoConstraints = false

    let badge = tag(text: index, textColor: UIColor(red: 0.94, green: 0.43, blue: 0.24, alpha: 1.0), background: UIColor(red: 1.0, green: 0.97, blue: 0.95, alpha: 1.0))
    let titleLabel = self.titleLabel(text: title, size: 20, weight: .bold)
    let bodyLabel = self.bodyLabel(text: body)

    container.addSubview(inner)
    inner.addArrangedSubview(badge)
    inner.addArrangedSubview(titleLabel)
    inner.addArrangedSubview(bodyLabel)

    NSLayoutConstraint.activate([
      inner.topAnchor.constraint(equalTo: container.topAnchor, constant: 18),
      inner.leadingAnchor.constraint(equalTo: container.leadingAnchor, constant: 18),
      inner.trailingAnchor.constraint(equalTo: container.trailingAnchor, constant: -18),
      inner.bottomAnchor.constraint(equalTo: container.bottomAnchor, constant: -18),
    ])

    return container
  }
}

@objc(NativeHybridViewManager)
class NativeHybridViewManager: RCTViewManager {
  override func view() -> UIView! {
    return NativeHybridView()
  }

  override static func requiresMainQueueSetup() -> Bool {
    true
  }
}

final class NativeHybridView: UIView {
  private let stackView = UIStackView()

  override init(frame: CGRect) {
    super.init(frame: frame)
    backgroundColor = UIColor(red: 0.99, green: 0.97, blue: 0.91, alpha: 1.0)

    stackView.translatesAutoresizingMaskIntoConstraints = false
    stackView.axis = .vertical
    stackView.spacing = 14
    stackView.alignment = .fill

    addSubview(stackView)

    NSLayoutConstraint.activate([
      stackView.topAnchor.constraint(equalTo: topAnchor),
      stackView.leadingAnchor.constraint(equalTo: leadingAnchor),
      stackView.trailingAnchor.constraint(equalTo: trailingAnchor),
      stackView.bottomAnchor.constraint(equalTo: bottomAnchor),
    ])

    stackView.addArrangedSubview(tag(text: "Swift Hybrid Native Views", textColor: UIColor(red: 0.06, green: 0.46, blue: 0.43, alpha: 1.0), background: UIColor(red: 0.89, green: 0.96, blue: 0.94, alpha: 1.0)))
    stackView.addArrangedSubview(titleLabel(text: "A compact Swift hybrid native views screen", size: 24, weight: .bold))
    stackView.addArrangedSubview(bodyLabel(text: "This screen combines React Native layout with fixed native iOS views, and it does not scroll."))
    stackView.addArrangedSubview(hybridRow(index: "01", title: "Timeline summary", body: "A native callout for the current event stage."))
    stackView.addArrangedSubview(hybridRow(index: "02", title: "Navigation handoff", body: "A second native row that keeps the flow compact and easy to scan."))
    stackView.addArrangedSubview(bodyLabel(text: "The next button sits below this native component so the app can continue back into the regular React Native flow."))
  }

  required init?(coder: NSCoder) {
    fatalError("init(coder:) has not been implemented")
  }

  private func hybridRow(index: String, title: String, body: String) -> UIView {
    let container = UIView()
    container.backgroundColor = .white
    container.layer.cornerRadius = 18
    container.layer.shadowColor = UIColor.black.cgColor
    container.layer.shadowOpacity = 0.05
    container.layer.shadowRadius = 10
    container.layer.shadowOffset = CGSize(width: 0, height: 5)

    let inner = UIStackView()
    inner.axis = .vertical
    inner.spacing = 10
    inner.translatesAutoresizingMaskIntoConstraints = false

    let badge = tag(text: index, textColor: UIColor(red: 0.06, green: 0.46, blue: 0.43, alpha: 1.0), background: UIColor(red: 0.93, green: 0.97, blue: 0.95, alpha: 1.0))
    let titleLabel = self.titleLabel(text: title, size: 18, weight: .bold)
    let bodyLabel = self.bodyLabel(text: body)

    container.addSubview(inner)
    inner.addArrangedSubview(badge)
    inner.addArrangedSubview(titleLabel)
    inner.addArrangedSubview(bodyLabel)

    NSLayoutConstraint.activate([
      inner.topAnchor.constraint(equalTo: container.topAnchor, constant: 16),
      inner.leadingAnchor.constraint(equalTo: container.leadingAnchor, constant: 16),
      inner.trailingAnchor.constraint(equalTo: container.trailingAnchor, constant: -16),
      inner.bottomAnchor.constraint(equalTo: container.bottomAnchor, constant: -16),
    ])

    return container
  }

  private func tag(text: String, textColor: UIColor, background: UIColor) -> UIView {
    let label = UILabel()
    label.text = text.uppercased()
    label.textColor = textColor
    label.backgroundColor = background
    label.font = .systemFont(ofSize: 11, weight: .bold)
    label.layer.cornerRadius = 10
    label.layer.masksToBounds = true
    label.textAlignment = .center
    label.numberOfLines = 1
    label.translatesAutoresizingMaskIntoConstraints = false
    let insets = UIEdgeInsets(top: 6, left: 12, bottom: 6, right: 12)
    return PaddingView(content: label, insets: insets)
  }

  private func titleLabel(text: String, size: CGFloat, weight: UIFont.Weight) -> UILabel {
    let label = UILabel()
    label.text = text
    label.textColor = UIColor(red: 0.09, green: 0.2, blue: 0.31, alpha: 1.0)
    label.font = .systemFont(ofSize: size, weight: weight)
    label.numberOfLines = 0
    return label
  }

  private func bodyLabel(text: String) -> UILabel {
    let label = UILabel()
    label.text = text
    label.textColor = UIColor(red: 0.30, green: 0.39, blue: 0.47, alpha: 1.0)
    label.font = .systemFont(ofSize: 15, weight: .regular)
    label.numberOfLines = 0
    label.setContentHuggingPriority(.defaultLow, for: .vertical)
    return label
  }
}

private final class PaddingView: UIView {
  private let content: UIView
  private let insets: UIEdgeInsets

  init(content: UIView, insets: UIEdgeInsets) {
    self.content = content
    self.insets = insets
    super.init(frame: .zero)
    addSubview(content)
    content.translatesAutoresizingMaskIntoConstraints = false
    NSLayoutConstraint.activate([
      content.topAnchor.constraint(equalTo: topAnchor, constant: insets.top),
      content.leadingAnchor.constraint(equalTo: leadingAnchor, constant: insets.left),
      content.trailingAnchor.constraint(equalTo: trailingAnchor, constant: -insets.right),
      content.bottomAnchor.constraint(equalTo: bottomAnchor, constant: -insets.bottom),
    ])
  }

  required init?(coder: NSCoder) {
    fatalError("init(coder:) has not been implemented")
  }
}

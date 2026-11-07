import shared
@testable import DevFest_Nantes

/// Hand-written fake of the Agenda port: its sessions and rooms come from scripted `FakeStream`s
/// (through the real `KotlinStreamBridge`). Classification delegates to the real shared use case
/// unless a test scripts the two flags.
final class FakeAgendaSource: AgendaSource {
    let sessionsStream: FakeStream<[Session]>
    let roomsStream: FakeStream<Set<Room>>
    private let classifier = ClassifySessionUseCase()
    private let talkOverride: ((Session) -> Bool)?
    private let navigableOverride: ((Session) -> Bool)?

    init(
        sessionsStream: FakeStream<[Session]> = FakeStream(.hang),
        roomsStream: FakeStream<Set<Room>> = FakeStream(.hang),
        talkOverride: ((Session) -> Bool)? = nil,
        navigableOverride: ((Session) -> Bool)? = nil
    ) {
        self.sessionsStream = sessionsStream
        self.roomsStream = roomsStream
        self.talkOverride = talkOverride
        self.navigableOverride = navigableOverride
    }

    func sessions() -> AsyncThrowingStream<[Session], Error> {
        sessionsStream.stream()
    }

    func rooms() -> AsyncThrowingStream<Set<Room>, Error> {
        roomsStream.stream()
    }

    func isTalk(_ session: Session) -> Bool {
        talkOverride?(session) ?? classifier.isTalk(session: session)
    }

    func isNavigable(_ session: Session) -> Bool {
        navigableOverride?(session) ?? classifier.isNavigable(session: session)
    }
}

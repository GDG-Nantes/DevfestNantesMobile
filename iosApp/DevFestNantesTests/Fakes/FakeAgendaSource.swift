import shared
@testable import DevFest_Nantes

/// Hand-written fake of the Agenda port: its sessions and rooms come from scripted `FakeStream`s
/// (through the real `KotlinStreamBridge`).
final class FakeAgendaSource: AgendaSource {
    let sessionsStream: FakeStream<[Session]>
    let roomsStream: FakeStream<Set<Room>>

    init(
        sessionsStream: FakeStream<[Session]> = FakeStream(.hang),
        roomsStream: FakeStream<Set<Room>> = FakeStream(.hang)
    ) {
        self.sessionsStream = sessionsStream
        self.roomsStream = roomsStream
    }

    func sessions() -> AsyncThrowingStream<[Session], Error> {
        sessionsStream.stream()
    }

    func rooms() -> AsyncThrowingStream<Set<Room>, Error> {
        roomsStream.stream()
    }
}

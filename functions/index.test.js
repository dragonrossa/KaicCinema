const { buildReservationDecisionMessage, handleReservationStatusChange } = require("./index");

describe("buildReservationDecisionMessage", () => {
  test("approved status returns approval message with movie title", () => {
    const message = buildReservationDecisionMessage("APPROVED", "Dune: Part Three");

    expect(message.title).toBe("Rezervacija odobrena");
    expect(message.body).toContain("Dune: Part Three");
    expect(message.body).toContain("odobrena");
  });

  test("rejected status returns rejection message with movie title", () => {
    const message = buildReservationDecisionMessage("REJECTED", "Dune: Part Three");

    expect(message.title).toBe("Rezervacija odbijena");
    expect(message.body).toContain("odbijena");
  });

  test("pending status returns null", () => {
    expect(buildReservationDecisionMessage("PENDING", "Dune: Part Three")).toBeNull();
  });
});

describe("handleReservationStatusChange", () => {
  function makeDeps({
    userExists = true,
    fcmToken = "token-123",
    screeningExists = true,
    movieTitle = "Dune: Part Three",
  } = {}) {
    const send = jest.fn().mockResolvedValue("message-id");
    const userDoc = { exists: userExists, data: () => ({ fcmToken }) };
    const screeningDoc = { exists: screeningExists, data: () => ({ movieTitle }) };
    const firestore = {
      collection: jest.fn((name) => ({
        doc: jest.fn(() => ({
          get: jest.fn().mockResolvedValue(name === "users" ? userDoc : screeningDoc),
        })),
      })),
    };
    return { firestore, messaging: { send } };
  }

  test("sends notification when status changes to APPROVED", async () => {
    const deps = makeDeps();

    await handleReservationStatusChange(
      { status: "PENDING" },
      { status: "APPROVED", userId: "user-1", screeningId: "screening-1" },
      deps,
    );

    expect(deps.messaging.send).toHaveBeenCalledTimes(1);
    const [[payload]] = deps.messaging.send.mock.calls;
    expect(payload.token).toBe("token-123");
    expect(payload.notification.body).toContain("Dune: Part Three");
    expect(payload.notification.body).toContain("odobrena");
  });

  test("sends notification when status changes to REJECTED", async () => {
    const deps = makeDeps();

    await handleReservationStatusChange(
      { status: "PENDING" },
      { status: "REJECTED", userId: "user-1", screeningId: "screening-1" },
      deps,
    );

    expect(deps.messaging.send).toHaveBeenCalledTimes(1);
    const [[payload]] = deps.messaging.send.mock.calls;
    expect(payload.notification.body).toContain("odbijena");
  });

  test("does not send when status is unchanged", async () => {
    const deps = makeDeps();

    await handleReservationStatusChange(
      { status: "APPROVED" },
      { status: "APPROVED", userId: "user-1", screeningId: "screening-1" },
      deps,
    );

    expect(deps.messaging.send).not.toHaveBeenCalled();
  });

  test("does not send when new status is still PENDING", async () => {
    const deps = makeDeps();

    await handleReservationStatusChange(
      { status: "PENDING" },
      { status: "PENDING", userId: "user-1", screeningId: "screening-1" },
      deps,
    );

    expect(deps.messaging.send).not.toHaveBeenCalled();
  });

  test("does not send when user has no fcm token", async () => {
    const deps = makeDeps({ fcmToken: null });

    await handleReservationStatusChange(
      { status: "PENDING" },
      { status: "APPROVED", userId: "user-1", screeningId: "screening-1" },
      deps,
    );

    expect(deps.messaging.send).not.toHaveBeenCalled();
  });

  test("does not send when user document does not exist", async () => {
    const deps = makeDeps({ userExists: false });

    await handleReservationStatusChange(
      { status: "PENDING" },
      { status: "APPROVED", userId: "user-1", screeningId: "screening-1" },
      deps,
    );

    expect(deps.messaging.send).not.toHaveBeenCalled();
  });

  test("falls back to generic wording when screening document is missing", async () => {
    const deps = makeDeps({ screeningExists: false });

    await handleReservationStatusChange(
      { status: "PENDING" },
      { status: "APPROVED", userId: "user-1", screeningId: "screening-1" },
      deps,
    );

    expect(deps.messaging.send).toHaveBeenCalledTimes(1);
    const [[payload]] = deps.messaging.send.mock.calls;
    expect(payload.notification.body).toContain("projekciju");
  });

  test("does nothing when before or after data is missing", async () => {
    const deps = makeDeps();

    await handleReservationStatusChange(undefined, { status: "APPROVED" }, deps);
    await handleReservationStatusChange({ status: "PENDING" }, undefined, deps);

    expect(deps.messaging.send).not.toHaveBeenCalled();
  });
});

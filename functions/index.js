const { onDocumentCreated, onDocumentUpdated } = require("firebase-functions/v2/firestore");
const { initializeApp } = require("firebase-admin/app");
const { getFirestore } = require("firebase-admin/firestore");
const { getMessaging } = require("firebase-admin/messaging");

initializeApp();

const APPROVED = "APPROVED";
const REJECTED = "REJECTED";
const DECISION_STATUSES = [APPROVED, REJECTED];
const NEW_SCREENINGS_TOPIC = "new_screenings";

function buildReservationDecisionMessage(status, movieTitle) {
  if (status === APPROVED) {
    return {
      title: "Rezervacija odobrena",
      body: `Vaša rezervacija za "${movieTitle}" je odobrena.`,
    };
  }
  if (status === REJECTED) {
    return {
      title: "Rezervacija odbijena",
      body: `Vaša rezervacija za "${movieTitle}" je odbijena.`,
    };
  }
  return null;
}

async function handleReservationStatusChange(before, after, { firestore, messaging }) {
  if (!before || !after || before.status === after.status) {
    return;
  }
  if (!DECISION_STATUSES.includes(after.status)) {
    return;
  }

  const [userSnapshot, screeningSnapshot] = await Promise.all([
    firestore.collection("users").doc(after.userId).get(),
    firestore.collection("screenings").doc(after.screeningId).get(),
  ]);

  const fcmToken = userSnapshot.exists ? userSnapshot.data().fcmToken : undefined;
  if (!fcmToken) {
    return;
  }

  const movieTitle = screeningSnapshot.exists ? screeningSnapshot.data().movieTitle : "projekciju";
  const message = buildReservationDecisionMessage(after.status, movieTitle);

  await messaging.send({
    token: fcmToken,
    notification: {
      title: message.title,
      body: message.body,
    },
  });
}

const onReservationStatusChange = onDocumentUpdated("reservations/{reservationId}", async (event) => {
  await handleReservationStatusChange(event.data.before.data(), event.data.after.data(), {
    firestore: getFirestore(),
    messaging: getMessaging(),
  });
});

function buildNewScreeningMessage(movieTitle) {
  return {
    title: "Nova projekcija",
    body: `Dodana je nova projekcija: "${movieTitle}".`,
  };
}

async function handleScreeningCreated(screening, { messaging }) {
  if (!screening || !screening.movieTitle) {
    return;
  }

  const message = buildNewScreeningMessage(screening.movieTitle);

  await messaging.send({
    topic: NEW_SCREENINGS_TOPIC,
    notification: {
      title: message.title,
      body: message.body,
    },
  });
}

const onScreeningCreated = onDocumentCreated("screenings/{screeningId}", async (event) => {
  await handleScreeningCreated(event.data.data(), {
    messaging: getMessaging(),
  });
});

module.exports = {
  buildReservationDecisionMessage,
  handleReservationStatusChange,
  onReservationStatusChange,
  buildNewScreeningMessage,
  handleScreeningCreated,
  onScreeningCreated,
  NEW_SCREENINGS_TOPIC,
};

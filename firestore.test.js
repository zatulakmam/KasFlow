const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";

const [emulatorHost, emulatorPortStr] = (
  process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085"
).split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated user cannot read or write transactions", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(
    unauthDb.collection("users").doc(ALICE_UID).collection("transactions").get()
  );
  await assertFails(
    unauthDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("transactions")
      .doc("tx_1")
      .set({
        userId: ALICE_UID,
        amount: 45000,
        type: "EXPENSE",
        category: "FOOD",
        date: Date.now() - 1000,
        title: "Makan Siang",
        note: "",
        yearMonth: "2026-11",
        createdAt: new Date(Date.now() - 1000),
        updatedAt: new Date(Date.now() - 1000),
      })
  );
});

test("Owner can create, list, partial update, and delete valid Transaction payloads", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const created = new Date(Date.now() - 10000);
  const txCollection = aliceDb
    .collection("users")
    .doc(ALICE_UID)
    .collection("transactions");

  await assertSucceeds(txCollection.orderBy("date", "desc").get());

  await assertSucceeds(
    txCollection.doc("tx_1").set({
      userId: ALICE_UID,
      amount: 8500000,
      type: "INCOME",
      category: "SALARY",
      date: Date.now() - 5000,
      title: "Gaji Bulanan",
      note: "Transfer kantor",
      yearMonth: "2026-11",
      createdAt: created,
      updatedAt: created,
    })
  );

  await assertSucceeds(
    txCollection.doc("tx_1").update({
      amount: 9200000,
      type: "INCOME",
      category: "SALARY",
      date: Date.now() - 4000,
      title: "Gaji & Tunjangan",
      note: "",
      yearMonth: "2026-11",
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  await assertSucceeds(txCollection.orderBy("date", "desc").get());
  await assertSucceeds(txCollection.doc("tx_1").delete());
});

test("Security guards reject ghost fields, @DocumentId id key, invalid amounts, and immutable edits", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const created = new Date(Date.now() - 10000);
  const txCollection = aliceDb
    .collection("users")
    .doc(ALICE_UID)
    .collection("transactions");

  await assertSucceeds(
    txCollection.doc("tx_valid").set({
      userId: ALICE_UID,
      amount: 28000,
      type: "EXPENSE",
      category: "FOOD",
      date: Date.now() - 5000,
      title: "Kopi Pagi",
      note: "",
      yearMonth: "2026-11",
      createdAt: created,
      updatedAt: created,
    })
  );

  await assertFails(
    txCollection.doc("tx_with_id").set({
      id: "tx_with_id",
      userId: ALICE_UID,
      amount: 15000,
      type: "EXPENSE",
      category: "FOOD",
      date: Date.now() - 1000,
      title: "Invalid ID",
      note: "",
      yearMonth: "2026-11",
      createdAt: created,
      updatedAt: created,
    })
  );

  await assertFails(
    txCollection.doc("tx_ghost").set({
      userId: ALICE_UID,
      amount: 15000,
      type: "EXPENSE",
      category: "FOOD",
      date: Date.now() - 1000,
      title: "Ghost",
      isAdmin: true,
      yearMonth: "2026-11",
      createdAt: created,
      updatedAt: created,
    })
  );

  await assertFails(
    txCollection.doc("tx_zero").set({
      userId: ALICE_UID,
      amount: 0,
      type: "EXPENSE",
      category: "FOOD",
      date: Date.now() - 1000,
      title: "Gratis",
      yearMonth: "2026-11",
      createdAt: created,
      updatedAt: created,
    })
  );

  await assertFails(
    txCollection.doc("tx_bad_ym").set({
      userId: ALICE_UID,
      amount: 10000,
      type: "EXPENSE",
      category: "FOOD",
      date: Date.now() - 1000,
      title: "Bulan Salah",
      yearMonth: "2026-13",
      createdAt: created,
      updatedAt: created,
    })
  );

  await assertFails(
    txCollection.doc("tx_future").set({
      userId: ALICE_UID,
      amount: 10000,
      type: "EXPENSE",
      category: "FOOD",
      date: Date.now() - 1000,
      title: "Masa Depan",
      yearMonth: "2026-11",
      createdAt: new Date(Date.now() + 60000),
      updatedAt: created,
    })
  );

  await assertFails(
    txCollection.doc("tx_valid").update({
      createdAt: new Date(Date.now() - 500),
      updatedAt: new Date(Date.now() - 500),
    })
  );

  await assertFails(
    txCollection.doc("tx_valid").update({
      userId: BOB_UID,
      updatedAt: new Date(Date.now() - 500),
    })
  );
});

test("Cross-user isolation: Alice cannot read, list, create, update, or delete Bob's transactions", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context
      .firestore()
      .collection("users")
      .doc(BOB_UID)
      .collection("transactions")
      .doc("bob_tx")
      .set({
        userId: BOB_UID,
        amount: 250000,
        type: "EXPENSE",
        category: "SHOPPING",
        date: Date.now() - 5000,
        title: "Belanja Bob",
        note: "Rahasia",
        yearMonth: "2026-11",
        createdAt: new Date(Date.now() - 10000),
        updatedAt: new Date(Date.now() - 10000),
      });
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobTxCol = aliceDb
    .collection("users")
    .doc(BOB_UID)
    .collection("transactions");

  await assertFails(bobTxCol.get());
  await assertFails(bobTxCol.doc("bob_tx").get());
  await assertFails(
    bobTxCol.doc("bob_tx").update({
      title: "Hijacked",
      updatedAt: new Date(Date.now() - 1000),
    })
  );
  await assertFails(bobTxCol.doc("bob_tx").delete());
  await assertFails(
    bobTxCol.doc("inject_tx").set({
      userId: ALICE_UID,
      amount: 10000,
      type: "EXPENSE",
      category: "FOOD",
      date: Date.now() - 1000,
      title: "Injected",
      yearMonth: "2026-11",
      createdAt: new Date(Date.now() - 1000),
      updatedAt: new Date(Date.now() - 1000),
    })
  );
});

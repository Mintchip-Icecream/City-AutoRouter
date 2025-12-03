BEGIN TRANSACTION;
CREATE TABLE IF NOT EXISTS "InterConditions" (
	"simID"	INTEGER NOT NULL,
	"interID"	INTEGER NOT NULL,
	"mapID"	INTEGER NOT NULL,
	"weatherRisk"	REAL DEFAULT 0,
	"obstacleRisk"	REAL DEFAULT 0,
	"trafficRisk"	REAL DEFAULT 0,
	PRIMARY KEY("interID","simID","mapID"),
	FOREIGN KEY("interID") REFERENCES "Intersections"("interID") ON DELETE CASCADE,
	FOREIGN KEY("mapID") REFERENCES "Intersections"("mapID") ON DELETE CASCADE,
	FOREIGN KEY("simID") REFERENCES "Simulation"("simID") ON DELETE CASCADE
);
CREATE TABLE IF NOT EXISTS "Intersections" (
	"interID"	INTEGER,
	"mapID"	INTEGER,
	"isLocation"	INTEGER DEFAULT 0,
	PRIMARY KEY("interID","mapID"),
	FOREIGN KEY("mapID") REFERENCES "Map"("mapID") ON DELETE CASCADE,
	CHECK("isLocation" IN (0, 1))
);
CREATE TABLE IF NOT EXISTS "Map" (
	"mapID"	INTEGER UNIQUE,
	"mapName"	TEXT NOT NULL,
	PRIMARY KEY("mapID" AUTOINCREMENT)
);
CREATE TABLE IF NOT EXISTS "Roads" (
	"sourceID"	INTEGER NOT NULL,
	"destinationID"	INTEGER NOT NULL,
	"mapID"	INTEGER NOT NULL,
	"roadLength"	REAL NOT NULL,
	"speedLimit"	REAL NOT NULL,
	"cardinalDirection"	TEXT NOT NULL,
	PRIMARY KEY("destinationID","sourceID","mapID"),
	FOREIGN KEY("destinationID") REFERENCES "Intersections"("interID") ON DELETE CASCADE,
	FOREIGN KEY("mapID") REFERENCES "Map"("mapID") ON DELETE CASCADE,
	FOREIGN KEY("sourceID") REFERENCES "Intersections"("interID") ON DELETE CASCADE,
	CHECK("cardinalDirection" IN ('NORTH', 'SOUTH', 'EAST', 'WEST'))
);
CREATE TABLE IF NOT EXISTS "RouteSequence" (
	"routeID"	INTEGER,
	"intersectionID"	INTEGER,
	"mapID"	INTEGER,
	"sequenceIndex"	INTEGER,
	PRIMARY KEY("routeID","intersectionID","mapID"),
	FOREIGN KEY("intersectionID") REFERENCES "Intersections"("interID") ON DELETE CASCADE,
	FOREIGN KEY("mapID") REFERENCES "Intersections"("mapID") ON DELETE CASCADE,
	FOREIGN KEY("routeID") REFERENCES "Routes"("routeID") ON DELETE CASCADE,
	CHECK("sequenceIndex" >= 0)
);
CREATE TABLE IF NOT EXISTS "Routes" (
	"routeID"	INTEGER,
	"lastUsed"	TEXT DEFAULT CURRENT_TIMESTAMP,
	PRIMARY KEY("routeID" AUTOINCREMENT),
	CHECK(datetime(lastUsed) IS NOT NULL)
);
CREATE TABLE IF NOT EXISTS "Simulation" (
	"simID"	INTEGER,
	"rngSeed"	INTEGER,
	"lastUsed"	TEXT DEFAULT CURRENT_TIMESTAMP,
	"timeCreated"	TEXT DEFAULT CURRENT_TIMESTAMP,
	PRIMARY KEY("simID" AUTOINCREMENT),
	CHECK(datetime("lastUsed") IS NOT NULL),
	CHECK(datetime("timeCreated") IS NOT NULL),
	CHECK(datetime("lastUsed") >= datetime("timeCreated"))
);
COMMIT;

CREATE TABLE `central_settings` (
	`id` int AUTO_INCREMENT NOT NULL,
	`centralId` varchar(80) NOT NULL,
	`name` varchar(120) NOT NULL,
	`city` varchar(120) NOT NULL,
	`phone` varchar(24) NOT NULL,
	`service` varchar(180) NOT NULL,
	`availability` varchar(120) NOT NULL,
	`responseTarget` varchar(180) NOT NULL,
	`status` enum('online','degraded','offline') NOT NULL DEFAULT 'online',
	`channelsJson` text NOT NULL,
	`updatedBy` int NOT NULL,
	`updatedAt` timestamp NOT NULL DEFAULT (now()) ON UPDATE CURRENT_TIMESTAMP,
	CONSTRAINT `central_settings_id` PRIMARY KEY(`id`),
	CONSTRAINT `central_settings_centralId_unique` UNIQUE(`centralId`)
);
--> statement-breakpoint
CREATE TABLE `sos_alerts` (
	`id` int AUTO_INCREMENT NOT NULL,
	`alertId` varchar(64) NOT NULL,
	`userId` int NOT NULL,
	`emergencyType` enum('security','medical','fire','accident','other') NOT NULL,
	`priority` enum('low','medium','high','critical') NOT NULL,
	`status` enum('received','dispatching','enroute','arrived','canceled','closed') NOT NULL DEFAULT 'received',
	`latitude` double,
	`longitude` double,
	`accuracy` double,
	`contactsQueued` int NOT NULL DEFAULT 0,
	`pushSent` int NOT NULL DEFAULT 0,
	`createdAt` timestamp NOT NULL DEFAULT (now()),
	`updatedAt` timestamp NOT NULL DEFAULT (now()) ON UPDATE CURRENT_TIMESTAMP,
	CONSTRAINT `sos_alerts_id` PRIMARY KEY(`id`),
	CONSTRAINT `sos_alerts_alertId_unique` UNIQUE(`alertId`)
);

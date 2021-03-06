# X-Sharing Services Router
The X-Sharing Services Router extends the [X-Sharing Services Integrator](https://github.com/RWTH-i5-IDSG/xsharing-services-integrator) to allow intermodal routing using traditional public transport and sharing services like car- and bikesharing. Combined with both the Integrator and the IVU AG public transportation router, it offers realistic intermodal itineraries, e.g. taking a bus to carsharing station and from there using the shared vehicle to the destination. It takes both real time public transportation information as well as real time sharing vehicles into consideration.

The Router is built as a Java EE (web) application and offers low latency interfaces based on Java Messaging Services (JMS). PostgreSQL is required as database backend.

The installation and configuration is described in [install.pdf](documentation/install.pdf) (German), the communication interface in [interface_ivu.pdf](documentation/interface_ivu.pdf) and [scenarios_v3.pdf](documentation/scenarios_v3.pdf) contains and overview over supported (and unsupported) use cases.

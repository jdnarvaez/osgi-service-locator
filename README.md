osgi-service-locator
====================

Small utility for quickly looking up OSGi services. The OSGiServiceLocator class can be used to lookup dynamic and declarative services within an OSGi environment. The bundle also automatically starts the Eclipse Equinox DS bundle (org.eclipse.equinox.ds) for environments where it has not automatically been started. Note that this class will not discover Eclipse style services that have been registered via Eclipse Service Factories. 

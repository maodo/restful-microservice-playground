package de.philipphauer.prozu;

import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.federecio.dropwizard.swagger.SwaggerBundle;
import io.federecio.dropwizard.swagger.SwaggerBundleConfiguration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hubspot.dropwizard.guice.GuiceBundle;

import de.philipphauer.prozu.configuration.ProZuConfiguration;

public class ProZuApplication extends Application<ProZuConfiguration> {

	private GuiceBundle<ProZuConfiguration> guiceBundle;
	private ProZuModule module;

	public static void main(String[] args) throws Exception {
		new ProZuApplication().run(args);
	}

	@Override
	public String getName() {
		return "prozu";
	}

	@Override
	public void initialize(Bootstrap<ProZuConfiguration> bootstrap) {
		ObjectMapper objectMapper = bootstrap.getObjectMapper();
		module = new ProZuModule(objectMapper);

		guiceBundle = GuiceBundle.<ProZuConfiguration> newBuilder()
				.addModule(module)
				.setConfigClass(ProZuConfiguration.class)
				.enableAutoConfig(getClass().getPackage().getName())
				.build();
		bootstrap.addBundle(guiceBundle);

		//accessible via http://localhost:<your_port>/swagger
		bootstrap.addBundle(new SwaggerBundle<ProZuConfiguration>() {
			protected SwaggerBundleConfiguration getSwaggerBundleConfiguration(ProZuConfiguration configuration) {
				return configuration.swaggerBundleConfiguration;
			}
		});
	}

	@Override
	public void run(ProZuConfiguration configuration, Environment environment) {
		// resources, healthchecks etc are automatically configured via guice bundle (enableAutoConfig())
	}

}

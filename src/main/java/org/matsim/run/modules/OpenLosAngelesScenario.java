/*-
 * #%L
 * MATSim Episim
 * %%
 * Copyright (C) 2020 matsim-org
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */
package org.matsim.run.modules;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.episim.EpisimConfigGroup;
import org.matsim.episim.policy.FixedPolicy;

/**
 * Scenario based on the publicly available OpenBerlin scenario (https://github.com/matsim-scenarios/matsim-berlin).
 */
public class OpenLosAngelesScenario extends AbstractModule {

	/**
	 * Activity names of the default params from {@link #addDefaultParams(EpisimConfigGroup)}.
	 */
	public static final String[] DEFAULT_ACTIVITIES = {
			"pt",
			"home", "work", "university", "school", "escort", "schoolescort", "schoolpureescort", "schoolridesharing", "non-schoolescort", 
			"maintenance", "HHmaintenance", "personalmaintenance", "eatout", "eatoutbreakfast", "eatoutlunch", "eatoutdinner",
			"visiting", "discretionary", "specialevent", "atwork", "atworkbusiness", "atworklunch", "atworkother",
			"freightStart", "freightEnd"
	};

	/**
	 * Adds default parameters that should be valid for most scenarios.
	 */
	public static void addDefaultParams(EpisimConfigGroup config) {		
		// pt
		config.addContainerParams(new EpisimConfigGroup.InfectionParams("pt", "tr"));
		// regular out-of-home acts:
		config.addContainerParams(new EpisimConfigGroup.InfectionParams("work"));
		config.addContainerParams(new EpisimConfigGroup.InfectionParams("school"));
		config.addContainerParams(new EpisimConfigGroup.InfectionParams("shop"));
		config.addContainerParams(new EpisimConfigGroup.InfectionParams("errands"));
		config.addContainerParams(new EpisimConfigGroup.InfectionParams("business"));
		config.addContainerParams(new EpisimConfigGroup.InfectionParams("university"));
		config.addContainerParams(new EpisimConfigGroup.InfectionParams("escort"));
		/*
		config.addContainerParams(new EpisimConfigGroup.InfectionParams("schoolescort"));
		config.addContainerParams(new EpisimConfigGroup.InfectionParams("schoolpureescort"));
		config.addContainerParams(new EpisimConfigGroup.InfectionParams("schoolridesharing"));
		config.addContainerParams(new EpisimConfigGroup.InfectionParams("non-schoolescort"));
		config.addContainerParams(new EpisimConfigGroup.InfectionParams("maintenance"));
		config.addContainerParams(new EpisimConfigGroup.InfectionParams("HHmaintenance"));
		config.addContainerParams(new EpisimConfigGroup.InfectionParams("personalmaintenance"));
		config.addContainerParams(new EpisimConfigGroup.InfectionParams("eatout"));
		config.addContainerParams(new EpisimConfigGroup.InfectionParams("eatoutbreakfast"));
		config.addContainerParams(new EpisimConfigGroup.InfectionParams("eatoutlunch"));
		config.addContainerParams(new EpisimConfigGroup.InfectionParams("eatoutdinner"));
		config.addContainerParams(new EpisimConfigGroup.InfectionParams("visiting"));
		config.addContainerParams(new EpisimConfigGroup.InfectionParams("discretionary"));
		config.addContainerParams(new EpisimConfigGroup.InfectionParams("specialevent"));
		config.addContainerParams(new EpisimConfigGroup.InfectionParams("atwork"));
		config.addContainerParams(new EpisimConfigGroup.InfectionParams("atworkbusiness"));
		config.addContainerParams(new EpisimConfigGroup.InfectionParams("atworklunch"));
		config.addContainerParams(new EpisimConfigGroup.InfectionParams("atworkother"));
		// freight act:
		config.addContainerParams(new EpisimConfigGroup.InfectionParams("freightStart"));
		config.addContainerParams(new EpisimConfigGroup.InfectionParams("freightEnd"));
		*/
		// home act:
		config.addContainerParams(new EpisimConfigGroup.InfectionParams("home"));
	}

	@Provides
	@Singleton
	public Config config() {

		Config config = ConfigUtils.createConfig(new EpisimConfigGroup());
		EpisimConfigGroup episimConfig = ConfigUtils.addOrGetModule(config, EpisimConfigGroup.class);

		config.network().setInputFile("~/Downloads/los-angeles-v1.0-network_2019-12-10.xml.gz");

		// String episimEvents_1pct = "../public-svn/matsim/scenarios/countries/de/berlin/berlin-v5.4-1pct-schools/output-berlin-v5.4-1pct-schools/berlin-v5.4-1pct-schools.output_events_for_episim.xml.gz";
		// String episimEvents_1pct = "../public-svn/matsim/scenarios/countries/de/berlin/berlin-v5.4-1pct/output-berlin-v5.4-1pct/berlin-v5.4-1pct.output_events_for_episim.xml.gz";
		// String episimEvents_10pct = "../public-svn/matsim/scenarios/countries/de/berlin/berlin-v5.4-10pct-schools/output-berlin-v5.4-10pct-schools/berlin-v5.4-10pct-schools.output_events_for_episim.xml.gz";

		String url = "https://svn.vsp.tu-berlin.de/repos/public-svn/matsim/scenarios/countries/us/los-angeles/los-angeles-v1.0/output/los-angeles-v1.1-1pct/los-angeles-v1.1-1pct.output_events.xml.gz";

		episimConfig.setInputEventsFile(url);

		episimConfig.setFacilitiesHandling(EpisimConfigGroup.FacilitiesHandling.bln);
		episimConfig.setSampleSize(0.01);
		episimConfig.setCalibrationParameter(2);
		//  episimConfig.setOutputEventsFolder("events");

		long closingIteration = 14;

		addDefaultParams(episimConfig);

		episimConfig.setPolicy(FixedPolicy.class, FixedPolicy.config()
				.shutdown(closingIteration, "leisure", "edu")
				.restrict(closingIteration, 0.2, "work", "business", "other")
				.restrict(closingIteration, 0.3, "shop", "errands")
				.restrict(closingIteration, 0.5, "pt")
				.open(closingIteration + 60, DEFAULT_ACTIVITIES)
				.build()
		);

		return config;
	}

}

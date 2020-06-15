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

import com.google.inject.Provides;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.episim.EpisimConfigGroup;
import org.matsim.episim.EpisimPerson;
import org.matsim.episim.EpisimUtils;
import org.matsim.episim.EpisimUtils.Extrapolation;
import org.matsim.episim.TracingConfigGroup;
import org.matsim.episim.model.FaceMask;
import org.matsim.episim.model.Transition;
import org.matsim.episim.policy.FixedPolicy;
import org.matsim.episim.policy.FixedPolicy.ConfigBuilder;
import org.matsim.episim.policy.Restriction;

import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Map;

import static org.matsim.episim.model.Transition.to;

/**
 * Snz scenario for Berlin.
 *
 * @see AbstractSnzScenario
 */
public class SnzBerlinScenario25pct2020 extends AbstractSnzScenario2020 {


	/**
	 * Path pointing to the input folder. Can be configured at runtime with EPISIM_INPUT variable.
	 */
	public static Path INPUT = EpisimUtils.resolveInputPath("../shared-svn/projects/episim/matsim-files/snz/BerlinV2/episim-input");

	public static class BasePolicyBuilder{
		private final EpisimConfigGroup episimConfig;
		private double alpha = 1.0;
		private double ciCorrection = 0.32;
		private String dateOfCiChange = "2020-03-08";
		private Extrapolation extrapolation = Extrapolation.linear;
		private Path csv = INPUT.resolve("BerlinSnzData_daily_until20200607.csv" );
		private long introductionPeriod = 14;
		private double clothFinalFraction = 0.5;
		private double surgicalFinalFraction = 0.1;
		public void setIntroductionPeriod( long introductionPeriod ){
			this.introductionPeriod = introductionPeriod;
		}
		public void setClothFinalFraction( double clothFinalFraction ){
			this.clothFinalFraction = clothFinalFraction;
		}
		public void setSurgicalFinalFraction( double surgicalFinalFraction ){
			this.surgicalFinalFraction = surgicalFinalFraction;
		}
		public BasePolicyBuilder( EpisimConfigGroup episimConfig ){
			this.episimConfig = episimConfig;
		}
		public void setCsv( Path csv ){
			this.csv = csv;
		}
		public double getAlpha(){
			return alpha;
		}
		public void setAlpha( double alpha ){
			this.alpha = alpha;
		}
		public double getCiCorrection(){
			return ciCorrection;
		}
		public void setCiCorrection( double ciCorrection ){
			this.ciCorrection = ciCorrection;
		}
		public String getDateOfCiChange(){
			return dateOfCiChange;
		}
		public void setDateOfCiChange( String dateOfCiChange ){
			this.dateOfCiChange = dateOfCiChange;
		}
		public Extrapolation getExtrapolation(){
			return extrapolation;
		}
		public void setExtrapolation( Extrapolation extrapolation ){
			this.extrapolation = extrapolation;
		}
		public ConfigBuilder build(){
			ConfigBuilder configBuilder = null;
			try {
				configBuilder = basePolicy(episimConfig, csv.toFile(), alpha, ciCorrection, dateOfCiChange, extrapolation, introductionPeriod,
						clothFinalFraction, surgicalFinalFraction );
			} catch ( IOException e) {
				throw new RuntimeException( e );
			}
			return configBuilder;
		}
	}

	/**
	 * The base policy based on actual restrictions in the past and mobility data
	 */
	private static FixedPolicy.ConfigBuilder basePolicy( EpisimConfigGroup episimConfig, File csv, double alpha,
							     double ciCorrection, String dateOfCiChange, Extrapolation extrapolation,
							     long introductionPeriod, Double clothFinalFraction, Double surgicalFinalFraction ) throws IOException {

		ConfigBuilder restrictions = EpisimUtils.createRestrictionsFromCSV2(episimConfig, csv, alpha, extrapolation);

		restrictions.restrict(dateOfCiChange, Restriction.ofCiCorrection(ciCorrection), AbstractSnzScenario2020.DEFAULT_ACTIVITIES);
		restrictions.restrict(dateOfCiChange, Restriction.ofCiCorrection(ciCorrection), "quarantine_home");
		restrictions.restrict(dateOfCiChange, Restriction.ofCiCorrection(ciCorrection), "pt");

		restrictions.restrict("2020-03-14", 0.1, "educ_primary", "educ_kiga")
				.restrict("2020-03-14", 0., "educ_secondary", "educ_higher", "educ_tertiary", "educ_other")
//				.restrict("2020-04-27", Restriction.ofMask(Map.of(FaceMask.CLOTH, 0.5, FaceMask.SURGICAL, 0.1)), "pt", "shop_daily", "shop_other")
				.restrict("2020-05-11", 0.3, "educ_primary")
				.restrict("2020-05-11", 0.2, "educ_secondary", "educ_higher", "educ_tertiary", "educ_other")
				.restrict("2020-05-25", 0.3, "educ_kiga")

//				.restrict("2020-06-08", 1., "educ_primary", "educ_kiga", "educ_secondary", "educ_higher", "educ_tertiary", "educ_other")
//
//				//Sommerferien
//				.restrict("2020-06-25", 0.3, "educ_primary", "educ_kiga")
//				.restrict("2020-06-25", 0.2, "educ_secondary", "educ_higher", "educ_tertiary", "educ_other")

				//Ende der Sommerferien
				.restrict("2020-08-10", 1., "educ_primary", "educ_kiga", "educ_secondary", "educ_higher", "educ_tertiary", "educ_other")
		;


		LocalDate masksCenterDate = LocalDate.of( 2020, 4,27);
		// this is the date when it was officially introduced in Berlin, so for the time being we do not make this configurable.  Might be different
		// in MUC and elsewhere!

		for ( int ii=0 ; ii<=introductionPeriod ; ii++ ) {
			LocalDate date = masksCenterDate.plusDays( -introductionPeriod/2+ii );
			restrictions.restrict(date, Restriction.ofMask(Map.of(FaceMask.CLOTH, clothFinalFraction*ii/introductionPeriod,
					FaceMask.SURGICAL,	surgicalFinalFraction*ii/introductionPeriod)), "pt", "shop_daily", "shop_other");
		}


		return restrictions;
	}

	/**
	 * Adds base progression config to the given builder.
	 */
	public static Transition.Builder baseProgressionConfig(Transition.Builder builder) {
		return builder
				// Inkubationszeit: Die Inkubationszeit [ ... ] liegt im Mittel (Median) bei 5–6 Tagen (Spannweite 1 bis 14 Tage)
				.from(EpisimPerson.DiseaseStatus.infectedButNotContagious,
						to(EpisimPerson.DiseaseStatus.contagious, Transition.logNormalWithMedianAndStd(4., 4.)))

// Dauer Infektiosität:: Es wurde geschätzt, dass eine relevante Infektiosität bereits zwei Tage vor Symptombeginn vorhanden ist und die höchste Infektiosität am Tag vor dem Symptombeginn liegt
// Dauer Infektiosität: Abstrichproben vom Rachen enthielten vermehrungsfähige Viren bis zum vierten, aus dem Sputum bis zum achten Tag nach Symptombeginn
				.from(EpisimPerson.DiseaseStatus.contagious,
						to(EpisimPerson.DiseaseStatus.showingSymptoms, Transition.logNormalWithMedianAndStd(2., 2.)),    //80%
						to(EpisimPerson.DiseaseStatus.recovered, Transition.logNormalWithMedianAndStd(4., 4.)))            //20%

// Erkankungsbeginn -> Hospitalisierung: Eine Studie aus Deutschland zu 50 Patienten mit eher schwereren Verläufen berichtete für alle Patienten eine mittlere (Median) Dauer von vier Tagen (IQR: 1–8 Tage)
				.from(EpisimPerson.DiseaseStatus.showingSymptoms,
						to(EpisimPerson.DiseaseStatus.seriouslySick, Transition.logNormalWithMedianAndStd(5., 5.)),
						to(EpisimPerson.DiseaseStatus.recovered, Transition.logNormalWithMedianAndStd(8., 8.)))

// Hospitalisierung -> ITS: In einer chinesischen Fallserie betrug diese Zeitspanne im Mittel (Median) einen Tag (IQR: 0–3 Tage)
				.from(EpisimPerson.DiseaseStatus.seriouslySick,
						to(EpisimPerson.DiseaseStatus.critical, Transition.logNormalWithMedianAndStd(1., 1.)),
						to(EpisimPerson.DiseaseStatus.recovered, Transition.logNormalWithMedianAndStd(14., 14.)))

// Dauer des Krankenhausaufenthalts: „WHO-China Joint Mission on Coronavirus Disease 2019“ wird berichtet, dass milde Fälle im Mittel (Median) einen Krankheitsverlauf von zwei Wochen haben und schwere von 3–6 Wochen
				.from(EpisimPerson.DiseaseStatus.critical,
						to(EpisimPerson.DiseaseStatus.seriouslySickAfterCritical, Transition.logNormalWithMedianAndStd(21., 21.)))

				.from(EpisimPerson.DiseaseStatus.seriouslySickAfterCritical,
						to(EpisimPerson.DiseaseStatus.recovered, Transition.logNormalWithMedianAndStd(7., 7.)))
				;

	}

	@Provides
	@Singleton
	public Config config() {

		Config config = getBaseConfig();

		EpisimConfigGroup episimConfig = ConfigUtils.addOrGetModule(config, EpisimConfigGroup.class);

		episimConfig.setInputEventsFile(INPUT.resolve("be_2020_snz_episim_events_25pt_split.xml.gz").toString());

		config.plans().setInputFile(INPUT.resolve("be_2020_snz_entirePopulation_emptyPlans_withDistricts_25pt_split.xml.gz").toString());

		episimConfig.setInitialInfections(500);
		episimConfig.setInitialInfectionDistrict("Berlin");
		episimConfig.setSampleSize(0.25);
		episimConfig.setCalibrationParameter(0.000_011_0);
		episimConfig.setMaxInteractions(3);
		String startDate = "2020-02-18";
		episimConfig.setStartDate(startDate);
		episimConfig.setHospitalFactor(1.8);
		episimConfig.setProgressionConfig(baseProgressionConfig(Transition.config()).build());

		TracingConfigGroup tracingConfig = ConfigUtils.addOrGetModule(config, TracingConfigGroup.class);
		int offset = (int) (ChronoUnit.DAYS.between(episimConfig.getStartDate(), LocalDate.parse("2020-04-01")) + 1);
		tracingConfig.setPutTraceablePersonsInQuarantineAfterDay(offset);
		double tracingProbability = 0.5;
		tracingConfig.setTracingProbability(tracingProbability);
		tracingConfig.setTracingPeriod_days(14);
		tracingConfig.setMinContactDuration_sec(15 * 60.);
		tracingConfig.setQuarantineHouseholdMembers(true);
		tracingConfig.setEquipmentRate(1.);
		tracingConfig.setTracingDelay_days(2);
		tracingConfig.setTracingCapacity_per_day(30);

		BasePolicyBuilder basePolicyBuilder = new BasePolicyBuilder( episimConfig );

		episimConfig.setPolicy(FixedPolicy.class, basePolicyBuilder.build().build() );

		config.controler().setOutputDirectory("./output-berlin-25pct-SNZrestrictsFromCSV-split-alpha-bmbf6-schools1-22-"+ basePolicyBuilder.getAlpha() + "-extrapolation-" + basePolicyBuilder.getExtrapolation() + "-ciCorrection-" + basePolicyBuilder.getCiCorrection() + "-dateOfCiChange-" + basePolicyBuilder.getDateOfCiChange() + "-startDate-" + episimConfig.getStartDate() + "-hospitalFactor-"+ episimConfig.getHospitalFactor() + "-calibrParam-" + episimConfig.getCalibrationParameter() + "-tracingProba-" + tracingProbability );

//		config.controler().setOutputDirectory("./output-berlin-25pct-unrestricted-calibr-" + episimConfig.getCalibrationParameter());

		return config;
	}

}

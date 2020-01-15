package simulation.models.enginegenerator;

import java.util.Map;
import java.util.Vector;
import java.util.concurrent.TimeUnit;

import fr.sorbonne_u.components.cyphy.interfaces.EmbeddingComponentStateAccessI;
import fr.sorbonne_u.devs_simulation.examples.molene.tic.TicEvent;
import fr.sorbonne_u.devs_simulation.hioa.annotations.ExportedVariable;
import fr.sorbonne_u.devs_simulation.hioa.models.AtomicHIOAwithEquations;
import fr.sorbonne_u.devs_simulation.hioa.models.vars.Value;
import fr.sorbonne_u.devs_simulation.interfaces.SimulationReportI;
import fr.sorbonne_u.devs_simulation.models.annotations.ModelExternalEvents;
import fr.sorbonne_u.devs_simulation.models.events.Event;
import fr.sorbonne_u.devs_simulation.models.events.EventI;
import fr.sorbonne_u.devs_simulation.models.time.Duration;
import fr.sorbonne_u.devs_simulation.models.time.Time;
import fr.sorbonne_u.devs_simulation.simulators.interfaces.SimulatorI;
import fr.sorbonne_u.devs_simulation.utils.AbstractSimulationReport;
import fr.sorbonne_u.utils.PlotterDescription;
import fr.sorbonne_u.utils.XYPlotter;
import simulation.events.enginegenerator.EngineGeneratorProductionEvent;
import simulation.events.enginegenerator.RefillEvent;
import simulation.events.enginegenerator.StartEvent;
import simulation.events.enginegenerator.StopEvent;
import simulation.tools.enginegenerator.EngineGeneratorState;
import wattwatt.tools.EngineGenerator.EngineGeneratorSetting;

@ModelExternalEvents(imported = { RefillEvent.class, StartEvent.class, StopEvent.class , TicEvent.class}, exported = {EngineGeneratorProductionEvent.class})
public class EngineGeneratorModel extends AtomicHIOAwithEquations {

	public static class GroupeElectrogeneModelReport extends AbstractSimulationReport {
		private static final long serialVersionUID = 1L;

		public GroupeElectrogeneModelReport(String modelURI) {
			super(modelURI);
		}

		/**
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return "GroupeElectrogeneModelReport(" + this.getModelURI() + ")";
		}
	}

	private static final long serialVersionUID = 1L;

	/**
	 * URI used to create instances of the model; assumes a singleton, otherwise a
	 * different URI must be given to each instance.
	 */
	public static final String URI = "EngineGeneratorModel";

	public static final String PRODUCTION = "production";
	public static final String QUANTITY = "quantity";
	
	public static final String PRODUCTION_SERIES = "production";
	public static final String QUANTITY_SERIES = "quantity";
	
	
	/** true when a external event triggered a reading. */
	protected boolean triggerReading;

	/** plotter for the production level over time. */
	protected XYPlotter productionPlotter;

	/** plotter for the fuel quantity over time. */
	protected XYPlotter fuelQuantityPlotter;

	/**
	 * reference on the object representing the component that holds the model;
	 * enables the model to access the state of this component.
	 */
	protected EmbeddingComponentStateAccessI componentRef;

	protected Value<Double> production = new Value<Double>(this, 0.0);

	protected EngineGeneratorState state;

	@ExportedVariable(type = Double.class)
	protected Value<Double> fuelCapacity = new Value<Double>(this, 10.0, 0);

	public EngineGeneratorModel(String uri, TimeUnit simulatedTimeUnit, SimulatorI simulationEngine)
			throws Exception {
		super(uri, simulatedTimeUnit, simulationEngine);
	}
	
	@Override
	public void			setSimulationRunParameters(
		Map<String, Object> simParams
		) throws Exception
	{
		String vname = this.getURI() + ":" + EngineGeneratorModel.PRODUCTION + ":"+ PlotterDescription.PLOTTING_PARAM_NAME ;
		PlotterDescription pdTemperature = (PlotterDescription) simParams.get(vname) ;
		this.productionPlotter = new XYPlotter(pdTemperature) ;
		this.productionPlotter.createSeries(EngineGeneratorModel.PRODUCTION_SERIES) ;
		
		vname = this.getURI() + ":" + EngineGeneratorModel.QUANTITY + ":"+ PlotterDescription.PLOTTING_PARAM_NAME ;
		PlotterDescription pdIntensity = (PlotterDescription) simParams.get(vname) ;
		this.fuelQuantityPlotter = new XYPlotter(pdIntensity) ;
		this.fuelQuantityPlotter.createSeries(QUANTITY_SERIES) ;
	}
	
	@Override
	public void			initialiseState(Time initialTime)
	{
		this.state = EngineGeneratorState.OFF ;
		this.triggerReading = false;
		
		if (this.productionPlotter != null) {
			this.productionPlotter.initialise() ;
			this.productionPlotter.showPlotter() ;
		}
		if (this.fuelQuantityPlotter != null) {
			this.fuelQuantityPlotter.initialise() ;
			this.fuelQuantityPlotter.showPlotter() ;
		}

		super.initialiseState(initialTime) ;
	}

	/**
	 * @see fr.sorbonne_u.devs_simulation.hioa.models.AtomicHIOA#initialiseVariables(fr.sorbonne_u.devs_simulation.models.time.Time)
	 */
	@Override
	protected void		initialiseVariables(Time startTime)
	{
		super.initialiseVariables(startTime);

		this.production.v = 0.0 ;
		this.fuelCapacity.v = EngineGeneratorSetting.FULL_CAPACITY;
	}



	@Override
	public Duration timeAdvance() {
		if (!this.triggerReading) {
			return Duration.INFINITY;
		} else {
			return Duration.zero(this.getSimulatedTimeUnit());
		}
	}

	@Override
	public Vector<EventI>	output()
	{
		if(this.triggerReading) {
			double reading = this.production.v ; // kW

			Vector<EventI> ret = new Vector<EventI>(1);
			Time currentTime = this.getCurrentStateTime().add(this.getNextTimeAdvance());
			EngineGeneratorProductionEvent production = new EngineGeneratorProductionEvent(currentTime, reading);
			ret.add(production);
			return ret;
		} else  {
			return null;
		}
	}
	
	/**
	 * @see fr.sorbonne_u.devs_simulation.models.AtomicModel#userDefinedInternalTransition(fr.sorbonne_u.devs_simulation.models.time.Duration)
	 */
	@Override
	public void userDefinedInternalTransition(Duration elapsedTime) {
		if (this.hasDebugLevel(1)) {
			this.logMessage("GroupeElectrogeneModel#userDefinedInternalTransition "
							+ elapsedTime) ;
		}
		if(this.triggerReading) {
			this.updateState();
			this.triggerReading = false;
		}
		if (elapsedTime.greaterThan(Duration.zero(getSimulatedTimeUnit()))) {
			super.userDefinedInternalTransition(elapsedTime) ;

			
		if (this.productionPlotter != null) {
			this.productionPlotter.addData(
				PRODUCTION,
				this.getCurrentStateTime().getSimulatedTime(), 
				this.production.v) ;
		}
		this.logMessage(this.getCurrentStateTime() +
				"|internal|production = " + this.production.v + " Watt") ;
		}
		if (this.fuelQuantityPlotter != null) {
			this.fuelQuantityPlotter.addData(
				QUANTITY,
				this.getCurrentStateTime().getSimulatedTime(), 
				this.fuelCapacity.v) ;
		}
		this.logMessage(this.getCurrentStateTime() +
				"|internal|temperature = " + this.fuelCapacity.v + " L") ;
		
	}

	/**
	 * @see fr.sorbonne_u.devs_simulation.models.AtomicModel#userDefinedExternalTransition(fr.sorbonne_u.devs_simulation.models.time.Duration)
	 */
	@Override
	public void userDefinedExternalTransition(Duration elapsedTime) {
		if (this.hasDebugLevel(2)) {
			this.logMessage("GroupeElectrogeneModel::userDefinedExternalTransition 1");
		}

		// get the vector of current external events
		Vector<EventI> currentEvents = this.getStoredEventAndReset();
		boolean ticReceived = false;
		// when this method is called, there is at least one external event
		assert currentEvents != null;

		Event ce = (Event) currentEvents.get(0);

		if (this.hasDebugLevel(2)) {
			this.logMessage("GroupeElectrogeneModel::userDefinedExternalTransition 2 " + ce.getClass().getCanonicalName());
		}

		// the plot is piecewise constant; this data will close the currently
		// open piece
		this.productionPlotter.addData(PRODUCTION, this.getCurrentStateTime().getSimulatedTime(), this.production.v);
		this.fuelQuantityPlotter.addData(QUANTITY, this.getCurrentStateTime().getSimulatedTime(), this.fuelCapacity.v);

		if (this.hasDebugLevel(2)) {
			this.logMessage("GroupeElectrogeneModel::userDefinedExternalTransition 3 " + this.state);
		}

		// execute the current external event on this model, changing its state
		// and intensity level
		if (ce instanceof TicEvent) {
			ticReceived = true;
			
		} else {
			ce.executeOn(this);
		}
		if (ticReceived) {
			this.triggerReading = true;
			this.logMessage(this.getCurrentStateTime() + "|external|tic event received.");
		}
		 if (this.hasDebugLevel(1)) {
		 this.logMessage("GroupeElectrogeneModel::userDefinedExternalTransition 4 ");
		 }

		// add a new data on the plotter; this data will open a new piece
		this.productionPlotter.addData(PRODUCTION, this.getCurrentStateTime().getSimulatedTime(), this.production.v);
		this.fuelQuantityPlotter.addData(QUANTITY, this.getCurrentStateTime().getSimulatedTime(), this.fuelCapacity.v);

		super.userDefinedExternalTransition(elapsedTime);
		if (this.hasDebugLevel(2)) {
			this.logMessage("GroupeElectrogeneModel::userDefinedExternalTransition 5");
		}
	}
	
	@Override
	public SimulationReportI getFinalReport() throws Exception {
		return new GroupeElectrogeneModelReport(this.getURI());
	}

	/**
	 * @see fr.sorbonne_u.devs_simulation.models.AtomicModel#endSimulation(fr.sorbonne_u.devs_simulation.models.time.Time)
	 */
	@Override
	public void endSimulation(Time endTime) throws Exception {
		this.productionPlotter.addData(PRODUCTION, this.getCurrentStateTime().getSimulatedTime(), this.production.v);
		this.fuelQuantityPlotter.addData(QUANTITY, this.getCurrentStateTime().getSimulatedTime(), this.fuelCapacity.v);
		super.endSimulation(endTime);
	}
	
	public void refill() {
		this.fuelCapacity.v = EngineGeneratorSetting.FULL_CAPACITY;
		updateState();
	}
	
	public void start() {
		this.state = EngineGeneratorState.ON;
		updateState();
		
	}
	
	public void stop() {
		this.state = EngineGeneratorState.OFF;
		updateState();
	}
	
	public boolean isOn() {
		return this.state == EngineGeneratorState.ON;
	}
	
	public boolean isFull() {
		return this.fuelCapacity.v == EngineGeneratorSetting.FUEL_CAPACITY;
	}
	
	public boolean isEmpty() {
		return this.fuelCapacity.v == 0.0;
	}
	
	public double getProduction() {
		return this.production.v;
	}
	
	public double getCapacity() {
		return this.fuelCapacity.v;
	}
	
	public void updateState() {
		if (this.isOn() && !this.isEmpty()) {
			this.production.v += EngineGeneratorSetting.PROD_THR;
			if (this.fuelCapacity.v - EngineGeneratorSetting.PROD_THR <= 0) {
				this.fuelCapacity.v = 0.0;
			} else {
				this.fuelCapacity.v -= EngineGeneratorSetting.PROD_THR;
			}
		} else {
			if (this.isEmpty()) {
				this.state = EngineGeneratorState.OFF;
				
			}
			this.production.v = 0.0;
		}
	}

}
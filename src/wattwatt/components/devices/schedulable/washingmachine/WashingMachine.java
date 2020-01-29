package wattwatt.components.devices.schedulable.washingmachine;

import java.util.Random;

import fr.sorbonne_u.components.annotations.OfferedInterfaces;
import fr.sorbonne_u.components.annotations.RequiredInterfaces;
import fr.sorbonne_u.components.cyphy.AbstractCyPhyComponent;
import fr.sorbonne_u.components.cyphy.interfaces.EmbeddingComponentAccessI;
import fr.sorbonne_u.components.exceptions.ComponentShutdownException;
import fr.sorbonne_u.components.exceptions.ComponentStartException;
import fr.sorbonne_u.devs_simulation.architectures.Architecture;
import simulation.models.washingmachine.WashingMachineCoupledModel;
import simulation.plugins.WashingMachineSimulatorPlugin;
import simulation.tools.washingmachine.WashingMachineState;
import wattwatt.interfaces.controller.IController;
import wattwatt.interfaces.devices.schedulable.washingmachine.IWashingMachine;
import wattwatt.ports.devices.schedulable.washingmachine.WashingMachineInPort;
import wattwatt.tools.washingmachine.WashingMachineMode;
import wattwatt.tools.washingmachine.WashingMachineSetting;

//-----------------------------------------------------------------------------
/**
* The class <code>WashingMachine</code>
*
* <p>
* <strong>Description</strong>
* </p>
* 
* ³ This class implements the washing machine component. The washing machine 
* requires the controller interface because he have to be
* connected to the controller to receive order from him.
* 
* 
* <p>
* Created on : 2020-01-27
* </p>
* 
* @author
*         <p>
*         Bah Thierno, Zheng Pascal
*         </p>
*/
//The next annotation requires that the referenced interface is added to
//the required interfaces of the component.
@OfferedInterfaces(offered = IWashingMachine.class)
@RequiredInterfaces(required = IController.class)
public class WashingMachine extends AbstractCyPhyComponent implements EmbeddingComponentAccessI {

	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------
	/** The inbound port of the washing machine */
	protected WashingMachineInPort lavein;

	/** The washing mode of the washing machine */
	protected WashingMachineMode mode;
	
	/** The time at which the washing machine will start */
	protected int startingTime;
	/** The duration of a washing cycle */
	protected int durationWork;

	/** The state of the washing machine */
	protected WashingMachineState state;
	
	/** The energy consumption of the washing machine */
	protected double conso;

	// -------------------------------------------------------------------------
	// Constants and variables used in the simulation
	// -------------------------------------------------------------------------
	protected boolean isOnSim;
	protected WashingMachineMode state2;
	protected boolean isWorkingSim;
	protected double consoSim;

	/** the simulation plug-in holding the simulation models. */
	protected WashingMachineSimulatorPlugin asp;

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	/**
	 * Create a washing machine.
	 * 
	 *
	 * @param uri        URI of the component.
	 * @param laveIn 	inbound port URI of the washing machine.
	 * @throws Exception <i>todo.</i>
	 */
	protected WashingMachine(String uri, String laveIn) throws Exception {
		super(uri, 2, 1);
		this.initialise();
		this.lavein = new WashingMachineInPort(laveIn, this);
		this.lavein.publishPort();
		
		
		this.state = WashingMachineState.OFF;
		ecoLavage();
		this.startingTime = WashingMachineSetting.START;
		this.tracer.setRelativePosition(1, 2);

	}

	protected void initialise() throws Exception {
		// The coupled model has been made able to create the simulation
		// architecture description.
		Architecture localArchitecture = this.createLocalArchitecture(null);
		// Create the appropriate DEVS simulation plug-in.
		this.asp = new WashingMachineSimulatorPlugin();
		// Set the URI of the plug-in, using the URI of its associated
		// simulation model.
		this.asp.setPluginURI(localArchitecture.getRootModelURI());
		// Set the simulation architecture.
		this.asp.setSimulationArchitecture(localArchitecture);
		// Install the plug-in on the component, starting its own life-cycle.
		this.installPlugin(this.asp);

		// Toggle logging on to get a log on the screen.
		this.toggleLogging();
	}
	
	// -------------------------------------------------------------------------
	// Methods
	// -------------------------------------------------------------------------

	@Override
	public void start() throws ComponentStartException {
		super.start();
		this.logMessage("LaveLinge starting");
		try {
			Thread.sleep(10);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void execute() throws Exception {
		super.execute();
//		SimulationEngine.SIMULATION_STEP_SLEEP_TIME = 10L;
//		HashMap<String, Object> simParams = new HashMap<String, Object>();
//		// Set the component ref to another key
//		simParams.put(URIS.WASHING_MACHINE_URI, this);
//		simParams.put(WashingMachineUserModel.URI + ":" + WashingMachineUserModel.MTBU,
//				WashingMachineUserBehaviour.MEAN_TIME_BETWEEN_USAGES);
//		simParams.put(WashingMachineUserModel.URI + ":" + WashingMachineUserModel.MTWE,
//				WashingMachineUserBehaviour.MEAN_TIME_WORKING_ECO);
//		simParams.put(WashingMachineUserModel.URI + ":" + WashingMachineUserModel.MTWP,
//				WashingMachineUserBehaviour.MEAN_TIME_WORKING_PREMIUM);
//		simParams.put(WashingMachineUserModel.URI + ":" + WashingMachineUserModel.STD, 10.0);
//
//		simParams.put(WashingMachineModel.URI + ":" + WashingMachineModel.CONSUMPTION_ECO,
//				WashingMachineSetting.CONSO_ECO_MODE_SIM);
//		simParams.put(WashingMachineModel.URI + ":" + WashingMachineModel.CONSUMPTION_PREMIUM,
//				WashingMachineSetting.CONSO_PREMIUM_MODE_SIM);
//		simParams.put(WashingMachineModel.URI + ":" + WashingMachineUserModel.STD, 10.0);
//
//		simParams.put(
//				WashingMachineUserModel.URI + ":" + WashingMachineUserModel.ACTION + ":"
//						+ PlotterDescription.PLOTTING_PARAM_NAME,
//				new PlotterDescription("LaveLingeUserModel", "Time (min)", "User actions", 0,
//						WattWattMain.getPlotterHeight(), WattWattMain.getPlotterWidth(),
//						WattWattMain.getPlotterHeight()));
//
//		simParams.put(
//				WashingMachineModel.URI + ":" + WashingMachineModel.INTENSITY_SERIES + ":"
//						+ PlotterDescription.PLOTTING_PARAM_NAME,
//				new PlotterDescription("LaveLingeModel", "Time (min)", "Consommation (W)", 0,
//						2 * WattWattMain.getPlotterHeight(), WattWattMain.getPlotterWidth(),
//						WattWattMain.getPlottstateerHeight()));
//		
//		this.asp.toggleDebugMode(); // Debug
//		this.asp.setSimulationRunParameters(simParams);
//		// Start the simulation.
//		this.runTask(new AbstractComponent.AbstractTask() {
//			@Override
//			public void run() {
//				try {
//					asp.doStandAloneSimulation(0.0, TimeScale.WEEK);
//				} catch (Exception e) {
//					throw new RuntimeException(e);
//				}
//			}
//		});
//
//		this.scheduleTaskAtFixedRate(new AbstractComponent.AbstractTask() {
//			@Override
//			public void run() {
//				try {
//					// not currently using thoses ////////////////////
//						((WashingMachine) this.getTaskOwner()).isOnSim = (((boolean) asp
//								.getModelStateValue(WashingMachineModel.URI, "isOn")));
//						((WashingMachine) this.getTaskOwner()).isWorkingSim = (((boolean) asp
//								.getModelStateValue(WashingMachineModel.URI, "isWorking")));
//						((WashingMachine) this.getTaskOwner()).consoSim = (((double) asp
//								.getModelStateValue(WashingMachineModel.URI, "consomation")));
//						((WashingMachine) this.getTaskOwner()).state = (((WashingMachineMode) asp
//								.getModelStateValue(WashingMachineModel.URI, "lavageMode")));
//					//////////////////////////////////////////////////
//				} catch (Exception e) {
//					throw new RuntimeException(e);
//				}
//			}
//		}, 0, ControllerSetting.UPDATE_RATE, TimeUnit.MILLISECONDS);
	}

	@Override
	public void shutdown() throws ComponentShutdownException {
		this.logMessage("LaveLinge shutdown");
		try {
			this.lavein.unpublishPort();
		} catch (Exception e) {
			e.printStackTrace();
		}
		super.shutdown();
	}

	@Override
	public void finalise() throws Exception {
		super.finalise();
	}

	@Override
	public Object getEmbeddingComponentStateValue(String name) throws Exception {
		if (name.equals("consumption")) {
			return new Double(this.conso);
		} else if (name.equals("mode")) {
			return this.mode;
		} else {
			
			assert name.equals("state");

			return this.state;
		}
	}
	
	@Override
	public void setEmbeddingComponentStateValue(String name, Object value) throws Exception {
		if (name.equals("startAt")) {
			int d = Math.round((float) value);
			
			this.startAt(d);
		} else if (name.equals("premiumMode")) {
			this.premiumLavage();
		} else {
			assert name.equals("ecoMode");
			this.ecoLavage();
		}
	}
	

	@Override
	protected Architecture createLocalArchitecture(String architectureURI) throws Exception {
		return WashingMachineCoupledModel.build();
	}
	

	public boolean canDelay(int delay) {
		return startingTime + delay < WashingMachineSetting.END;
	}

	public boolean canAdvance(int advance) {
		return startingTime - advance >= WashingMachineSetting.START;
	}

	public int durationWork() {
		return durationWork;
	}

	public int startingTime() {
		return startingTime;
	}

	public int endingTime() {
		return startingTime + durationWork;
	}

	public void endBefore(int end) {
		startingTime = end;
	}

	public void startAt(int debut) {
		startingTime = debut;
		this.state = WashingMachineState.WORKING;
	}

	public void late(int delay) {
		if (canDelay(delay)) {
			startingTime += delay;
		}
	}

	public void advance(int advance) {
		if (canAdvance(advance)) {
			startingTime -= advance;
		}
	}

	public void On() {
		this.state = WashingMachineState.ON;
	}

	public void Off() {
		this.state = WashingMachineState.OFF;
	}

	public boolean isOn() {
		return this.state == WashingMachineState.ON ||this.state == WashingMachineState.WORKING; 
	}

	public boolean isWorking() {
		return this.state == WashingMachineState.WORKING;
	}

	public double giveConso() {
		if (isOn()) {
			return conso;
		} else {
			return 0;
		}
	}

	public void ecoLavage() {
		mode = WashingMachineMode.ECO;
		conso = WashingMachineSetting.CONSO_ECO_MODE;
		this.state = WashingMachineState.OFF;
		durationWork = WashingMachineSetting.DURATION_ECO_MODE;
	}

	public void premiumLavage() {
		mode = WashingMachineMode.PREMIUM;
		conso = WashingMachineSetting.CONSO_PREMIUM_MODE;
		this.state = WashingMachineState.OFF;
		durationWork = WashingMachineSetting.CONSO_PREMIUM_MODE;
	}

	public WashingMachineMode getMode() {
		return mode;
	}

	public void behave(Random rand) throws InterruptedException {
		if (this.state == WashingMachineState.ON) {
			this.state = WashingMachineState.WORKING;
			if (getMode() == WashingMachineMode.ECO) {
				this.logMessage(
						"Washing machine starting eco mode at: " + this.startingTime + " for " + this.durationWork);
				Thread.sleep(this.durationWork / 2);
				this.logMessage("Still working for " + this.durationWork / 2);
				Thread.sleep(this.durationWork / 2);
			} else {
				this.logMessage(
						"Washing machine starting premium mode at: " + this.startingTime + " for " + this.durationWork);
				Thread.sleep(this.durationWork / 2);
				this.logMessage("Still working for " + this.durationWork / 2);
				Thread.sleep(this.durationWork / 2);
			}
			this.state = WashingMachineState.OFF;
		}
	}

	public void printState() {
		this.logMessage(">>> isOn : [" + this.state + "] Mode : [" + this.getMode()
				+ " ] \n>>> Conso depuis le debut : [" + this.giveConso() + " ]\n");
	}

	

}

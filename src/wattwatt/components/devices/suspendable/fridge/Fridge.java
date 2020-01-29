package wattwatt.components.devices.suspendable.fridge;

import fr.sorbonne_u.components.annotations.OfferedInterfaces;
import fr.sorbonne_u.components.annotations.RequiredInterfaces;
import fr.sorbonne_u.components.cyphy.AbstractCyPhyComponent;
import fr.sorbonne_u.components.cyphy.interfaces.EmbeddingComponentAccessI;
import fr.sorbonne_u.components.exceptions.ComponentShutdownException;
import fr.sorbonne_u.components.exceptions.ComponentStartException;
import fr.sorbonne_u.devs_simulation.architectures.Architecture;
import fr.sorbonne_u.devs_simulation.simulators.SimulationEngine;
import simulation.models.fridge.FridgeCoupledModel;
import simulation.models.fridge.FridgeModel;
import simulation.models.hairdryer.HairDryerModel;
import simulation.plugins.FridgeSimulatorPlugin;
import simulation.tools.fridge.FridgeConsumption;
import simulation.tools.fridge.FridgeDoor;
import wattwatt.interfaces.controller.IController;
import wattwatt.interfaces.devices.suspendable.fridge.IFridge;
import wattwatt.ports.devices.suspendable.fridge.FridgeInPort;
import wattwatt.tools.fridge.FridgeSetting;

//-----------------------------------------------------------------------------
/**
* The class <code>Fridge</code>
*
* <p>
* <strong>Description</strong>
* </p>
* 
* This class implements the fridge component. The fridge 
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
@OfferedInterfaces(offered = IFridge.class)
@RequiredInterfaces(required = IController.class)
public class Fridge extends AbstractCyPhyComponent implements EmbeddingComponentAccessI {

	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------
	/** The inbound port of the fridge */
	protected FridgeInPort refrin;
	
	/** The mode of the fridge, if he is suspended or not */
	protected FridgeConsumption consumptionState;
	
	/** The current state of the fridge door */
	protected FridgeDoor currentDoorState;
	
	protected double intensity;
	
	/** The temperature of the fridge used in the simulation */
	protected double temperature;

	/** The temperature of the upper compartment */
	protected double tempH;
	/** The temperature of the lower compartment */
	protected double tempB;

	/** The state of the fridge */
	protected boolean isOn;
	/** The state of the fridge */
	protected boolean isWorking;
	
	/** The energy consumption of the fridge */
	protected double conso;


	/** the simulation plug-in holding the simulation models. */
	protected FridgeSimulatorPlugin asp;

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	/**
	 * Create a washing machine.
	 * 
	 *
	 * @param uri        URI of the component.
	 * @param refriIn 	inbound port URI of the fridge.
	 * @throws Exception <i>todo.</i>
	 */
	protected Fridge(String uri, String refriIn) throws Exception {
		super(uri, 2, 1);
		this.initialise();
		this.refrin = new FridgeInPort(refriIn, this);
		this.refrin.publishPort();

		this.tempH = FridgeSetting.TEMP_H_INIT;
		this.tempB = FridgeSetting.TEMP_L_INIT;

		this.consumptionState = FridgeConsumption.SUSPENDED;
		this.currentDoorState = FridgeDoor.CLOSED;
		this.intensity = 0.0;
		this.temperature = (double) this.asp.getModelStateValue(FridgeModel.URI, "temperature");
		this.on();
		this.tracer.setRelativePosition(1, 0);
	}

	protected void initialise() throws Exception {
		// The coupled model has been made able to create the simulation
		// architecture description.
		Architecture localArchitecture = this.createLocalArchitecture(null);
		// Create the appropriate DEVS simulation plug-in.
		this.asp = new FridgeSimulatorPlugin();
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
		this.logMessage("Refrigerateur starting");
		try {
			Thread.sleep(10);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void execute() throws Exception {
		super.execute();
		SimulationEngine.SIMULATION_STEP_SLEEP_TIME = 10L;
		// To give an example of the embedding component access facility, the
		// following lines show how to set the reference to the embedding
		// component or a proxy responding to the access calls.
//		HashMap<String, Object> simParams = new HashMap<String, Object>();
//		simParams.put(URIS.FRIDGE_URI, this);
//		simParams.put(FridgeUserModel.URI + ":" + FridgeUserModel.MTBI, 200.0) ;
//		simParams.put(FridgeUserModel.URI + ":" + FridgeUserModel.MID, 10.0) ;
//		simParams.put(
//				FridgeUserModel.URI + ":" + PlotterDescription.PLOTTING_PARAM_NAME,
//				new PlotterDescription(
//						"RefrigerateurUserModel",
//						"Time (min)",
//						"Opened / Closed",
//						WattWattMain.getPlotterWidth(),
//						0,
//						WattWattMain.getPlotterWidth(),
//						WattWattMain.getPlotterHeight())) ;
//		
//		simParams.put(
//				FridgeModel.URI + ":" + FridgeModel.MAX_TEMPERATURE, 5.0) ;
//		simParams.put(
//				FridgeModel.URI + ":" + FridgeModel.MIN_TEMPERATURE, 1.0) ;
//		simParams.put(FridgeModel.URI + ":" + FridgeModel.INITIAL_TEMP, 3.0) ;
//		simParams.put(
//				FridgeModel.URI + ":" + FridgeModel.TEMPERATURE + ":" + PlotterDescription.PLOTTING_PARAM_NAME,
//				new PlotterDescription(
//						"RefrigerateurModel",
//						"Time (min)",
//						"Temperature (Celcius)",
//						WattWattMain.getPlotterWidth(),
//						WattWattMain.getPlotterHeight(),
//						WattWattMain.getPlotterWidth(),
//						WattWattMain.getPlotterHeight())) ;
//		simParams.put(
//				FridgeModel.URI + ":"  + FridgeModel.INTENSITY + ":" + PlotterDescription.PLOTTING_PARAM_NAME,
//				new PlotterDescription(
//						"RefrigerateurModel",
//						"Time (min)",
//						"Intensity (W)",
//						WattWattMain.getPlotterWidth(),
//						2*WattWattMain.getPlotterHeight(),
//						WattWattMain.getPlotterWidth(),
//						WattWattMain.getPlotterHeight())) ;
//
//		simParams.put(
//				FridgeSensorModel.URI + ":" + FridgeModel.MAX_TEMPERATURE, 2.5) ;
//		simParams.put(
//				FridgeSensorModel.URI + ":" + FridgeModel.MIN_TEMPERATURE, 1.0) ;
//		simParams.put(
//				FridgeSensorModel.URI + ":" + PlotterDescription.PLOTTING_PARAM_NAME,
//				new PlotterDescription(
//						"RefrigerateurSensorModel",
//						"Time (min)",
//						"Temperature (Celcius)",
//						WattWattMain.getPlotterWidth(),
//						3*WattWattMain.getPlotterHeight(),
//						WattWattMain.getPlotterWidth(),
//						WattWattMain.getPlotterHeight())) ;
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
//		this.scheduleTaskAtFixedRate(new AbstractComponent.AbstractTask() {
//			@Override
//			public void run() {
//				try {
//						((Fridge) this.getTaskOwner()).setDoorState(
//								(((FridgeDoor) asp.getModelStateValue(FridgeModel.URI, "door"))));
//						((Fridge) this.getTaskOwner()).setConsumptionState((((FridgeConsumption) asp
//								.getModelStateValue(FridgeModel.URI, "consumption"))));
//						((Fridge) this.getTaskOwner()).setTemperature(
//								((double) asp.getModelStateValue(FridgeModel.URI, "temperature")));
//						((Fridge) this.getTaskOwner())
//								.setIntensity(((double) asp.getModelStateValue(FridgeModel.URI, "intensity")));
//				} catch (Exception e) {
//					throw new RuntimeException(e);
//				}
//			}
//		}, 0, ControllerSetting.UPDATE_RATE, TimeUnit.MILLISECONDS);
	}

	@Override
	public void shutdown() throws ComponentShutdownException {
		this.logMessage("Refrigerateur shutdown");
		try {
			this.refrin.unpublishPort();
		} catch (Exception e) {
			e.printStackTrace();
		}
		super.shutdown();
	}

	@Override
	public void finalise() throws Exception {
		super.finalise();
	}

	// -------------------------------------------------------------------------
	// Methods
	// -------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.components.cyphy.AbstractCyPhyComponent#createLocalArchitecture(java.lang.String)
	 */
	@Override
	protected Architecture createLocalArchitecture(String architectureURI) throws Exception {
		return FridgeCoupledModel.build();
	}

	/**
	 * @see fr.sorbonne_u.components.cyphy.interfaces.EmbeddingComponentStateAccessI#getEmbeddingComponentStateValue(java.lang.String)
	 */
	@Override
	public Object getEmbeddingComponentStateValue(String name) throws Exception {
		if (name.equals("door")) {
			return this.asp.getModelStateValue(HairDryerModel.URI, "door");
		} else if (name.equals("consumption")) {
			return this.asp.getModelStateValue(HairDryerModel.URI, "consumption");
		} else if (name.equals("temperature")) {
			return this.asp.getModelStateValue(HairDryerModel.URI, "temperature");
		} else {
			assert name.equals("intensity");
			return this.asp.getModelStateValue(HairDryerModel.URI, "intensity");
		}
	}
	
	@Override
	public void setEmbeddingComponentStateValue(String name, Object value) throws Exception {
		// TODO Auto-generated method stub
		EmbeddingComponentAccessI.super.setEmbeddingComponentStateValue(name, value);
	}
	
	public double getTempHaut() {
		return this.tempH;
	}

	public double getTempBas() {
		return this.tempB;
	}

	public void suspend() {
		this.isWorking = false;
	}

	public void resume() {
		if (this.isOn) {
			this.isWorking = true;
		} else {
			this.isWorking = false;
		}

	}

	public void on() {
		this.isOn = true;
		this.isWorking = true;
	}

	public void off() {
		this.isOn = false;
		this.isWorking = false;
	}

	public boolean isWorking() {
		return this.isWorking;
	}

	public boolean isOn() {
		return this.isOn;
	}

	public double giveConso() {
		return conso;
	}

	public void setDoorState(FridgeDoor door) {
		this.currentDoorState = door;
	}

	public void setConsumptionState(FridgeConsumption consumption) {
		this.consumptionState = consumption;
	}

	public void setIntensity(double intensity) {
		this.intensity = intensity;
	}

	public void setTemperature(double temperature) {
		this.temperature = temperature;
	}

	public void regule() {
		if (this.isOn) {
			if (this.isWorking) {
				if (this.tempH > FridgeSetting.TEMP_H_MIN) {
					this.tempH--;
				}
				if (this.tempB > FridgeSetting.TEMP_L_MIN) {
					this.tempB--;
				}
				this.conso += FridgeSetting.ACTIVE_CONSUMPTION;
			} else {
				if (this.tempH < FridgeSetting.TEMP_H_MAX) {
					this.tempH++;
				}
				if (this.tempB < FridgeSetting.TEMP_L_MAX) {
					this.tempB++;
				}
				if (this.conso - FridgeSetting.PASSIVE_CONSUMPTION <= 0) {
					this.conso = 0;
				} else {
					this.conso -= FridgeSetting.PASSIVE_CONSUMPTION;
				}

			}
		} else {
			if (this.conso - FridgeSetting.PASSIVE_CONSUMPTION <= 0) {
				this.conso = 0;
			} else {
				this.conso -= FridgeSetting.PASSIVE_CONSUMPTION;
			}
		}
	}

	public void printState() {
		this.logMessage(">>> isOn : [" + this.isOn + "] Working : [" + this.isWorking + "] Temp Haut : ["
				+ this.getTempHaut() + " ] Temp Bas : [" + this.getTempBas() + " ] \n>>> Conso depuis le debut : ["
				+ this.giveConso() + " ]\n");
	}

	
}

package simulation.models.washingmachine;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import fr.sorbonne_u.devs_simulation.architectures.Architecture;
import fr.sorbonne_u.devs_simulation.architectures.SimulationEngineCreationMode;
import fr.sorbonne_u.devs_simulation.examples.molene.tic.TicEvent;
import fr.sorbonne_u.devs_simulation.examples.molene.tic.TicModel;
import fr.sorbonne_u.devs_simulation.hioa.architectures.AtomicHIOA_Descriptor;
import fr.sorbonne_u.devs_simulation.hioa.architectures.CoupledHIOA_Descriptor;
import fr.sorbonne_u.devs_simulation.hioa.models.vars.StaticVariableDescriptor;
import fr.sorbonne_u.devs_simulation.hioa.models.vars.VariableSink;
import fr.sorbonne_u.devs_simulation.hioa.models.vars.VariableSource;
import fr.sorbonne_u.devs_simulation.interfaces.ModelDescriptionI;
import fr.sorbonne_u.devs_simulation.interfaces.SimulationReportI;
import fr.sorbonne_u.devs_simulation.models.CoupledModel;
import fr.sorbonne_u.devs_simulation.models.architectures.AbstractAtomicModelDescriptor;
import fr.sorbonne_u.devs_simulation.models.architectures.AtomicModelDescriptor;
import fr.sorbonne_u.devs_simulation.models.architectures.CoupledModelDescriptor;
import fr.sorbonne_u.devs_simulation.models.events.EventI;
import fr.sorbonne_u.devs_simulation.models.events.EventSink;
import fr.sorbonne_u.devs_simulation.models.events.EventSource;
import fr.sorbonne_u.devs_simulation.models.events.ReexportedEvent;
import fr.sorbonne_u.devs_simulation.simulators.interfaces.SimulatorI;
import fr.sorbonne_u.devs_simulation.utils.StandardCoupledModelReport;
import simulation.events.washingmachine.EcoModeEvent;
import simulation.events.washingmachine.PremiumModeEvent;
import simulation.events.washingmachine.StartAtEvent;

public class WashingMachineCoupledModel extends CoupledModel {

	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	private static final long serialVersionUID = 1L;
	/** URI of the unique instance of this class (in this example). */
	public static final String URI = "WashingMachineCoupledModel";

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	public WashingMachineCoupledModel(String uri, TimeUnit simulatedTimeUnit, SimulatorI simulationEngine,
			ModelDescriptionI[] submodels, Map<Class<? extends EventI>, EventSink[]> imported,
			Map<Class<? extends EventI>, ReexportedEvent> reexported, Map<EventSource, EventSink[]> connections,
			Map<StaticVariableDescriptor, VariableSink[]> importedVars,
			Map<VariableSource, StaticVariableDescriptor> reexportedVars, Map<VariableSource, VariableSink[]> bindings)
			throws Exception {
		super(uri, simulatedTimeUnit, simulationEngine, submodels, imported, reexported, connections, importedVars,
				reexportedVars, bindings);
	}
	
	/**
	 * @see fr.sorbonne_u.devs_simulation.models.CoupledModel#getFinalReport()
	 */
	@Override
	public SimulationReportI getFinalReport() throws Exception {
		StandardCoupledModelReport ret = new StandardCoupledModelReport(this.getURI());
		for (int i = 0; i < this.submodels.length; i++) {
			ret.addReport(this.submodels[i].getFinalReport());
		}
		return ret;
	}

	public static Architecture build() throws Exception {
		Map<String, AbstractAtomicModelDescriptor> atomicModelDescriptors = new HashMap<>();

		atomicModelDescriptors.put(WashingMachineModel.URI,
				AtomicHIOA_Descriptor.create(WashingMachineModel.class, WashingMachineModel.URI, TimeUnit.SECONDS,
						null, SimulationEngineCreationMode.ATOMIC_ENGINE));

		atomicModelDescriptors.put(WashingMachineUserModel.URI,
				AtomicModelDescriptor.create(WashingMachineUserModel.class, WashingMachineUserModel.URI,
						TimeUnit.SECONDS, null, SimulationEngineCreationMode.ATOMIC_ENGINE));
		
		atomicModelDescriptors.put(TicModel.URI + "-3", AtomicModelDescriptor.create(TicModel.class,
				TicModel.URI + "-3", TimeUnit.SECONDS, null, SimulationEngineCreationMode.ATOMIC_ENGINE));

		Map<String, CoupledModelDescriptor> coupledModelDescriptors = new HashMap<String, CoupledModelDescriptor>();

		Set<String> submodels = new HashSet<String>();
		submodels.add(WashingMachineModel.URI);
		submodels.add(WashingMachineUserModel.URI);
		submodels.add(TicModel.URI + "-3");

		Map<EventSource, EventSink[]> connections = new HashMap<EventSource, EventSink[]>();
		EventSource from1 = new EventSource(WashingMachineUserModel.URI, StartAtEvent.class);
		EventSink[] to1 = new EventSink[] { new EventSink(WashingMachineModel.URI, StartAtEvent.class) };
		connections.put(from1, to1);
		EventSource from2 = new EventSource(WashingMachineUserModel.URI, EcoModeEvent.class);
		EventSink[] to2 = new EventSink[] { new EventSink(WashingMachineModel.URI, EcoModeEvent.class) };
		connections.put(from2, to2);
		EventSource from3 = new EventSource(WashingMachineUserModel.URI, PremiumModeEvent.class);
		EventSink[] to3 = new EventSink[] { new EventSink(WashingMachineModel.URI, PremiumModeEvent.class) };
		connections.put(from3, to3);
		
		EventSource from5 = new EventSource(TicModel.URI + "-3", TicEvent.class);
		EventSink[] to5 = new EventSink[] { new EventSink(WashingMachineModel.URI, TicEvent.class) };
		connections.put(from5, to5);
		
		/*Map<VariableSource, VariableSink[]> bindings = new HashMap<VariableSource, VariableSink[]>();
		VariableSource source = new VariableSource("fuelCapacity", Double.class, GroupeElectrogeneModel.URI);
		VariableSink[] sinks = new VariableSink[] {
				new VariableSink("fuelCapacity", Double.class, GroupeElectrogeneUserModel.URI) };
		bindings.put(source, sinks);*/

		coupledModelDescriptors.put(WashingMachineCoupledModel.URI,
				new CoupledHIOA_Descriptor(WashingMachineCoupledModel.class, WashingMachineCoupledModel.URI, submodels,
						null, null, connections, null, SimulationEngineCreationMode.COORDINATION_ENGINE, null, null,
						null));

		return new Architecture(WashingMachineCoupledModel.URI, atomicModelDescriptors, coupledModelDescriptors,
				TimeUnit.SECONDS);
	}

}
package pipeline.outoforder;

import generic.Event;
import generic.RequestType;
import generic.SimulationElement;

public class MispredictionPenaltyCompleteEvent extends Event {

	public MispredictionPenaltyCompleteEvent(long eventTime,
			SimulationElement requestingElement,
			SimulationElement processingElement, RequestType requestType) {
		super(null, eventTime, requestingElement, processingElement, requestType, -1);
	}

}

/*
 * Copyright 2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"). You may not use this file except in compliance
 * with the License. A copy of the License is located at
 *
 * http://aws.amazon.com/apache2.0/
 *
 * or in the "license" file accompanying this file. This file is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */
package ai.djl.training.evaluator;

import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDList;
import ai.djl.ndarray.types.DataType;
import ai.djl.util.Pair;

/** {@link Accuracy} is the {@link AbstractAccuracy} with multiple classes. */
public class Accuracy extends AbstractAccuracy {

    /**
     * Creates a multiclass accuracy evaluator that computes accuracy across axis 1 along the 0th
     * index.
     */
    public Accuracy() {}

    /**
     * Creates a multiclass accuracy evaluator that computes accuracy across axis 1 along given
     * index.
     *
     * @param name the name of the evaluator, default is "Accuracy"
     * @param index the index of the NDArray in labels to compute accuracy for
     */
    public Accuracy(String name, int index) {
        super(name, index);
    }

    /**
     * Creates a multiclass accuracy evaluator.
     *
     * @param name the name of the evaluator, default is "Accuracy"
     * @param index the index of the NDArray in labels to compute accuracy for
     * @param axis the axis that represent classes in prediction, default 1
     */
    public Accuracy(String name, int index, int axis) {
        super(name, index, axis);
    }

    /** {@inheritDoc} */
    @Override
    protected Pair<Long, NDArray> accuracyHelper(NDList labels, NDList predictions) {
        if (labels.size() != predictions.size()) {
            throw new IllegalArgumentException("labels and prediction length does not match.");
        }
        NDArray label = labels.get(index);
        NDArray prediction = predictions.get(index);
        checkLabelShapes(label, prediction);
        NDArray predictionReduced;
        if (!label.getShape().equals(prediction.getShape())) {
            // Multi-class, sparse label
            predictionReduced = prediction.argMax(axis);
        } else {
            // Multi-class, one-hot label
            predictionReduced = prediction;
        }
        // result of sum is int64 now
        long total = label.size();
        NDArray correct =
                label.toType(DataType.INT64, false)
                        .eq(predictionReduced.toType(DataType.INT64, false))
                        .countNonzero();
        return new Pair<>(total, correct);
    }
}

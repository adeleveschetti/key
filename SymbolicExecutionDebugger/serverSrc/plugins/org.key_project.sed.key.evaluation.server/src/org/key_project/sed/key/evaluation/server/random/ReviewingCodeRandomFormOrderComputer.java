package org.key_project.sed.key.evaluation.server.random;

import java.io.File;
import java.io.FileInputStream;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.key_project.sed.key.evaluation.model.definition.RandomForm;
import org.key_project.sed.key.evaluation.model.definition.ReviewingCodeEvaluation;
import org.key_project.sed.key.evaluation.model.definition.Tool;
import org.key_project.sed.key.evaluation.model.input.AbstractFormInput;
import org.key_project.sed.key.evaluation.model.input.AbstractPageInput;
import org.key_project.sed.key.evaluation.model.input.EvaluationInput;
import org.key_project.sed.key.evaluation.model.input.RandomFormInput;
import org.key_project.sed.key.evaluation.model.input.ToolPageInput;
import org.key_project.sed.key.evaluation.model.io.EvaluationInputReader;
import org.key_project.sed.key.evaluation.server.index.PermutationIndex;
import org.key_project.sed.key.evaluation.server.index.PermutationIndex.Entry;
import org.key_project.sed.key.evaluation.server.index.PermutationIndex.IDataFactory;
import org.key_project.sed.key.evaluation.server.index.PermutationIndex.IEntryUpdater;
import org.key_project.sed.key.evaluation.server.io.FileStorage;
import org.key_project.util.java.ArrayUtil;
import org.key_project.util.java.CollectionUtil;
import org.key_project.util.java.ObjectUtil;

/**
 * The {@link IRandomCompletion} used by the {@link ReviewingCodeEvaluation}.
 * @author Martin Hentschel
 */
public class ReviewingCodeRandomFormOrderComputer extends AbstractRandomCompletion {
   /**
    * The used {@link BalancingEntry}.
    */
   private BalancingEntry balancingEntry;

   /**
    * Constructor.
    * @param storageLocation The storage location providing existing evaluation inputs.
    */
   public ReviewingCodeRandomFormOrderComputer(File storageLocation) {
      String[] elements = {ReviewingCodeEvaluation.EXAMPLE_1_PAGE_NAME, 
                           ReviewingCodeEvaluation.EXAMPLE_2_PAGE_NAME, 
                           ReviewingCodeEvaluation.EXAMPLE_3_PAGE_NAME, 
                           ReviewingCodeEvaluation.EXAMPLE_4_PAGE_NAME, 
                           ReviewingCodeEvaluation.EXAMPLE_5_PAGE_NAME, 
                           ReviewingCodeEvaluation.EXAMPLE_6_PAGE_NAME};
      // Analyze existing documents
      final Map<String, IndexData> existingDataMap = new HashMap<String, IndexData>();
      File[] instructionFiles = FileStorage.listFormFiles(storageLocation, ReviewingCodeEvaluation.INSTANCE.getName(), ReviewingCodeEvaluation.INTRODUCTION_FORM_NAME);
      if (!ArrayUtil.isEmpty(instructionFiles)) {
         for (File file : instructionFiles) {
            try {
               EvaluationInput evaluationInput = EvaluationInputReader.parse(new FileInputStream(file));
               RandomFormInput evaluationFormInput = (RandomFormInput)evaluationInput.getFormInput(evaluationInput.getEvaluation().getForm(ReviewingCodeEvaluation.EVALUATION_FORM_NAME));
               String permutationKey = null;
               List<Tool> toolOrder = new LinkedList<Tool>();
               if (evaluationFormInput.getPageOrder() != null) {
                  // Analyze page order
                  for (AbstractPageInput<?> pageInput : evaluationFormInput.getPageOrder()) {
                     String pageName = pageInput.getPage().getName();
                     if (ArrayUtil.contains(elements, pageName)) {
                        toolOrder.add(evaluationFormInput.getTool(pageInput));
                        if (permutationKey == null) {
                           permutationKey = pageName;
                        }
                        else {
                           permutationKey += "," + pageName;
                        }
                     }
                  }
                  // Get or create PermutationData
                  IndexData data = existingDataMap.get(permutationKey);
                  if (data == null) {
                     data = new IndexData();
                     existingDataMap.put(permutationKey, data);
                  }
                  // Update PermutationData
                  if (isToolUsedFirst(toolOrder, ReviewingCodeEvaluation.NO_TOOL_NAME, ReviewingCodeEvaluation.SED_TOOL_NAME, 6)) {
                     data.increaseNoToolCount();
                     if (isCompleted(storageLocation, evaluationInput)) {
                        data.increaseNoToolCompletedCount();
                     }
                  }
                  else if (isToolUsedFirst(toolOrder, ReviewingCodeEvaluation.SED_TOOL_NAME, ReviewingCodeEvaluation.NO_TOOL_NAME, 6)) {
                     data.increaseSedCount();
                     if (isCompleted(storageLocation, evaluationInput)) {
                        data.increaseSedCompletedCount();
                     }
                  }
               }
            }
            catch (Exception e) {
               e.printStackTrace();
            }
         }
      }
      // Create balancing index instances
      IDataFactory<String, IndexData> dataFactory = new IDataFactory<String, IndexData>() {
         @Override
         public IndexData createData(String[] permutation) {
            String key = ArrayUtil.toString(permutation, ",");
            IndexData existingData =  existingDataMap.remove(key);
            if (existingData != null) {
               return existingData;
            }
            else {
               return new IndexData();
            }
         }
      };
      IndexDataComparator dataComparator = new IndexDataComparator();
      PermutationIndex<String, IndexData> permutationIndex = new PermutationIndex<String, IndexData>(elements, dataFactory, dataComparator);
      int noToolCountTotal = 0;
      int sedCountTotal = 0;
      for (Entry<String, IndexData> indexEntry : permutationIndex.getIndex()) {
         IndexData indexData = indexEntry.getData();
         noToolCountTotal += indexData.getNoToolCount();
         sedCountTotal += indexData.getSedCount();
      }
      balancingEntry = new BalancingEntry(permutationIndex, noToolCountTotal, sedCountTotal);
   }

   /**
    * Checks if the given {@link EvaluationInput} is completed meaning
    * that the evaluation form is also available.
    * @param storageLocation The storage location.
    * @param introductionInput The {@link EvaluationInput} of the introduction form.
    * @return {@code true} is completed, {@code false} is not completed.
    */
   protected boolean isCompleted(File storageLocation, EvaluationInput introductionInput) {
      try {
         File evaluationFile = FileStorage.getFile(storageLocation, ReviewingCodeEvaluation.INSTANCE.getName(), ReviewingCodeEvaluation.EVALUATION_FORM_NAME, introductionInput.getUUID());
         if (evaluationFile != null) {
            EvaluationInput evaluationInput = EvaluationInputReader.parse(new FileInputStream(evaluationFile));
            return ObjectUtil.equals(evaluationInput.getUUID(), introductionInput.getUUID());
         }
         else {
            return false;
         }
      }
      catch (Exception e) {
         return false; // Treat unparsable files as not completed.
      }
   }

   /**
    * Returns the used {@link BalancingEntry}.
    * @return The used {@link BalancingEntry}.
    */
   public BalancingEntry getBalancingEntry() {
      return balancingEntry;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public List<RandomFormInput> computeRandomValues(EvaluationInput evaluationInput, AbstractFormInput<?> currentForm) {
      try {
         // Update index and compute which order should be returned.
         BalancingEntryUpdater updater = new BalancingEntryUpdater(balancingEntry);
         balancingEntry.getPermutationIndex().updateFirstEntry(updater);
         // Create order
         return computeOrder(evaluationInput, currentForm, updater.getPermutation(), updater.isNoToolFirst());
      }
      catch (Exception e) { // In case of an exception return a fixed order as fallback.
         e.printStackTrace();
         return computeFixedOrder(evaluationInput, currentForm, true, false);
      }
   }
   
   /**
    * An {@link IEntryUpdater} used to update a {@link BalancingEntry}.
    * @author Martin Hentschel
    */
   protected static class BalancingEntryUpdater implements IEntryUpdater<String, IndexData> {
      /**
       * The {@link BalancingEntry} to modify.
       */
      private final BalancingEntry balancingEntry;
      
      /**
       * The permutation to use.
       */
      private String[] permutation;

      /**
       * Use NO_TOOL first?
       */
      private boolean noToolFirst;
      
      /**
       * Constructor.
       * @param balancingEntry The {@link BalancingEntry} to modify.
       */
      public BalancingEntryUpdater(BalancingEntry balancingEntry) {
         this.balancingEntry = balancingEntry;
      }

      /**
       * {@inheritDoc}
       */
      @Override
      public boolean updateFirstEntry(Entry<String, IndexData> firstEntry) {
         permutation = firstEntry.getPermutation();
         IndexData indexData = firstEntry.getData();
         boolean indexUpdateRequired = false;
         // Completed count is ignored for simplicity.
         if (indexData.getNoToolCount() < indexData.getSedCount()) {
            noToolFirst = true;
            indexUpdateRequired = true;
         }
         else if (indexData.getNoToolCount() > indexData.getSedCount()) {
            noToolFirst = false;
            indexUpdateRequired = true;
         }
         else {
            noToolFirst = balancingEntry.noToolCountTotal < balancingEntry.sedCountTotal;
         }
         // Update balancing entry
         if (noToolFirst) {
            indexData.increaseNoToolCount();
            balancingEntry.noToolCountTotal++;
         }
         else {
            indexData.increaseSedCount();
            balancingEntry.sedCountTotal++;
         }
         return indexUpdateRequired;
      }

      /**
       * Returns the permutation to use.
       * @return The permutation to use.
       */
      public String[] getPermutation() {
         return permutation;
      }

      /**
       * Checks if NO_TOOL should be used first.
       * @return {@code true} NO_TOOL first, {@code false} SED first.
       */
      public boolean isNoToolFirst() {
         return noToolFirst;
      }
   }
   
   /**
    * Computes a single fixed order.
    * @param evaluationInput The {@link EvaluationInput}.
    * @param currentForm The current {@link AbstractFormInput}.
    * @param noToolFirst Use NO_TOOL as first tool?
    * @param reverseOrder Reverse fixed order?
    * @return The fixed order.
    */
   public static List<RandomFormInput> computeFixedOrder(EvaluationInput evaluationInput, 
                                                         AbstractFormInput<?> currentForm,
                                                         boolean noToolFirst,
                                                         boolean reverseOrder) {
      String[] order = reverseOrder ?
                       new String[] {ReviewingCodeEvaluation.EXAMPLE_4_PAGE_NAME, ReviewingCodeEvaluation.EXAMPLE_5_PAGE_NAME, ReviewingCodeEvaluation.EXAMPLE_6_PAGE_NAME, ReviewingCodeEvaluation.EXAMPLE_1_PAGE_NAME, ReviewingCodeEvaluation.EXAMPLE_2_PAGE_NAME, ReviewingCodeEvaluation.EXAMPLE_3_PAGE_NAME} :
                       new String[] {ReviewingCodeEvaluation.EXAMPLE_3_PAGE_NAME, ReviewingCodeEvaluation.EXAMPLE_2_PAGE_NAME, ReviewingCodeEvaluation.EXAMPLE_1_PAGE_NAME, ReviewingCodeEvaluation.EXAMPLE_6_PAGE_NAME, ReviewingCodeEvaluation.EXAMPLE_5_PAGE_NAME, ReviewingCodeEvaluation.EXAMPLE_4_PAGE_NAME};
      return computeOrder(evaluationInput, currentForm, order, noToolFirst);
   }
   
   /**
    * Computes the order.
    * @param evaluationInput The {@link EvaluationInput}.
    * @param currentForm The current {@link AbstractFormInput}.
    * @param exampleOrder The order of the examples.
    * @param noToolFirst Use no tool as first tool?
    * @return The computed order.
    */
   @SuppressWarnings("unchecked")
   public static List<RandomFormInput> computeOrder(EvaluationInput evaluationInput, 
                                                    AbstractFormInput<?> currentForm,
                                                    String[] exampleOrder,
                                                    boolean noToolFirst) {
      // Get needed objects
      RandomForm evaluationForm = ((ReviewingCodeEvaluation) evaluationInput.getEvaluation()).getEvaluationForm();
      RandomFormInput evaluationFormInput = (RandomFormInput) evaluationInput.getFormInput(evaluationForm);
      AbstractPageInput<?> evaluationPage = evaluationFormInput.getPageInput(ReviewingCodeEvaluation.EVALUATION_PAGE_NAME);
      AbstractPageInput<?> jmlPage = evaluationFormInput.getPageInput(ReviewingCodeEvaluation.JML_PAGE_NAME);
      ToolPageInput tool1Page;
      ToolPageInput tool2Page;
      if (noToolFirst) {
         tool1Page = (ToolPageInput) evaluationFormInput.getPageInput(ReviewingCodeEvaluation.NO_TOOL_NAME);
         tool2Page = (ToolPageInput) evaluationFormInput.getPageInput(ReviewingCodeEvaluation.SED_TOOL_NAME);
      }
      else {
         tool1Page = (ToolPageInput) evaluationFormInput.getPageInput(ReviewingCodeEvaluation.SED_TOOL_NAME);
         tool2Page = (ToolPageInput) evaluationFormInput.getPageInput(ReviewingCodeEvaluation.NO_TOOL_NAME);
      }
      AbstractPageInput<?> proof1Page = evaluationFormInput.getPageInput(exampleOrder[0]);
      AbstractPageInput<?> proof2Page = evaluationFormInput.getPageInput(exampleOrder[1]);
      AbstractPageInput<?> proof3Page = evaluationFormInput.getPageInput(exampleOrder[2]);
      AbstractPageInput<?> proof4Page = evaluationFormInput.getPageInput(exampleOrder[3]);
      AbstractPageInput<?> proof5Page = evaluationFormInput.getPageInput(exampleOrder[4]);
      AbstractPageInput<?> proof6Page = evaluationFormInput.getPageInput(exampleOrder[5]);
      AbstractPageInput<?> feedbackPage = evaluationFormInput.getPageInput(ReviewingCodeEvaluation.FEEDBACK_PAGE);
      AbstractPageInput<?> sendPage = evaluationFormInput.getPageInput(ReviewingCodeEvaluation.SEND_EVALUATION_PAGE_NAME);
      // Set order and tools
      evaluationFormInput.setPageOrder(CollectionUtil.toList(evaluationPage, 
                                                             jmlPage, 
                                                             tool1Page, 
                                                             proof1Page, 
                                                             proof2Page, 
                                                             proof3Page, 
                                                             tool2Page, 
                                                             proof4Page, 
                                                             proof5Page, 
                                                             proof6Page, 
                                                             feedbackPage, 
                                                             sendPage));
      evaluationFormInput.setTool(proof1Page, tool1Page.getPage().getTool());
      evaluationFormInput.setTool(proof2Page, tool1Page.getPage().getTool());
      evaluationFormInput.setTool(proof3Page, tool1Page.getPage().getTool());
      evaluationFormInput.setTool(proof4Page, tool2Page.getPage().getTool());
      evaluationFormInput.setTool(proof5Page, tool2Page.getPage().getTool());
      evaluationFormInput.setTool(proof6Page, tool2Page.getPage().getTool());
      return CollectionUtil.toList(evaluationFormInput);
   }
   
   /**
    * The {@link Comparator} used to compare {@link IndexData} instances.
    * @author Martin Hentschel
    */
   public static class IndexDataComparator implements Comparator<IndexData> {
      /**
       * {@inheritDoc}
       */
      @Override
      public int compare(IndexData o1, IndexData o2) {
         // Compare balanced state (NO_TOOL use equal to SED use), completed count is ignored for simplicity
         boolean o1balanced = o1.getNoToolCount() == o1.getSedCount();
         boolean o2balanced = o2.getNoToolCount() == o2.getSedCount();
         if (o1balanced && o2balanced) {
            return compareCounts(o1, o2); 
         }
         else if (!o1balanced && o2balanced) {
            return -1;
         }
         else if (o1balanced && !o2balanced) {
            return 1;
         }
         else {
            return compareCounts(o1, o2); 
         }
      }
      
      /**
       * Compares NO_TOOL and SED count.
       * @param o1 The first {@link IndexData}.
       * @param o2 The second {@link IndexData}.
       * @return The comparison result.
       */
      protected int compareCounts(IndexData o1, IndexData o2) {
         if (o1.getNoToolCount() < o2.getNoToolCount() && o1.getSedCount() < o2.getSedCount()) {
            return -1;
         }
         else if (o1.getNoToolCount() > o2.getNoToolCount() && o1.getSedCount() > o2.getSedCount()) {
            return 1;
         }
         else {
            int o1max = Math.max(o1.getNoToolCount(), o1.getSedCount());
            int o2max = Math.max(o2.getNoToolCount(), o2.getSedCount());
            if (o1max < o2max) {
               return -1;
            }
            else if (o1max > o2max) {
               return 1;
            }
            else {
               int o1min = Math.min(o1.getNoToolCount(), o1.getSedCount());
               int o2min = Math.min(o2.getNoToolCount(), o2.getSedCount());
               if (o1min < o2min) {
                  return -1;
               }
               else if (o1min > o2min) {
                  return 1;
               }
               else {
                  return 0;
               }
            }
         }
      }
   }

   /**
    * The data stored in the used {@link PermutationIndex}.
    * @author Martin Hentschel
    */
   public static class IndexData {
      /**
       * Counts how often NO_TOOL was used first.
       */
      private int noToolCount;

      /**
       * Counts how often SED was used first.
       */
      private int sedCount;

      /**
       * Counts how often NO_TOOL is completed.
       */
      private int noToolCompletedCount;

      /**
       * Counts how often SED is completed.
       */
      private int sedCompletedCount;
      
      /**
       * Constructor.
       */
      public IndexData() {
         this(0, 0, 0, 0);
      }
      
      /**
       * Constructor.
       * @param noToolCount The NO_TOOL used first counter.
       * @param sedCount The SED used first counter.
       * @param noToolCompletedCount The NO_TOOL completed counter.
       * @param sedCompletedCount The SED completed counter.
       */
      public IndexData(int noToolCount, int sedCount, int noToolCompletedCount, int sedCompletedCount) {
         this.noToolCount = noToolCount;
         this.sedCount = sedCount;
         this.noToolCompletedCount = noToolCompletedCount;
         this.sedCompletedCount = sedCompletedCount;
      }

      /**
       * Increases the NO_TOOL used first counter by {@code 1}.
       */
      protected void increaseNoToolCount() {
         noToolCount++;
      }
      
      /**
       * Increases the SED used first counter by {@code 1}.
       */
      protected void increaseSedCount() {
         sedCount++;
      }
      
      /**
       * Increases the NO_TOOL completed counter by {@code 1}.
       */
      protected void increaseNoToolCompletedCount() {
         noToolCompletedCount++;
      }
      
      /**
       * Increases the SED completed counter by {@code 1}.
       */
      protected void increaseSedCompletedCount() {
         sedCompletedCount++;
      }

      /**
       * Returns the NO_TOOL used first counter.
       * @return The NO_TOOL used first counter.
       */
      public int getNoToolCount() {
         return noToolCount;
      }

      /**
       * Returns the SED used first counter.
       * @return The SED used first counter.
       */
      public int getSedCount() {
         return sedCount;
      }

      /**
       * Returns the NO_TOOL completed counter.
       * @return The NO_TOOL completed counter.
       */
      public int getNoToolCompletedCount() {
         return noToolCompletedCount;
      }

      /**
       * Returns the SED completed counter.
       * @return The SED completed counter.
       */
      public int getSedCompletedCount() {
         return sedCompletedCount;
      }

      /**
       * {@inheritDoc}
       */
      @Override
      public String toString() {
         return "NO_TOOL Count = " + noToolCount + 
                 ", NO_TOOL Completed Count = " + noToolCompletedCount +
                 ", SED Count = " + sedCount +
                 ", SED Completed Count = " + sedCompletedCount;
      }
   }
   
   /**
    * Provides all information relevant for balancing.
    * @author Martin Hentschel
    */
   public static class BalancingEntry {
      /**
       * The used {@link PermutationIndex}.
       */
      private final PermutationIndex<String, IndexData> permutationIndex;
      
      /**
       * The total amount of NO_TOOL count.
       */
      private int noToolCountTotal;
      
      /**
       * The total amount of SED count.
       */
      private int sedCountTotal;

      /**
       * Constructor.
       * @param permutationIndex The used {@link PermutationIndex}.
       * @param noToolCountTotal The total amount of NO_TOOL count.
       * @param sedCountTotal The total amount of SED count.
       */
      public BalancingEntry(PermutationIndex<String, IndexData> permutationIndex, int noToolCountTotal, int sedCountTotal) {
         this.permutationIndex = permutationIndex;
         this.noToolCountTotal = noToolCountTotal;
         this.sedCountTotal = sedCountTotal;
      }

      /**
       * Returns the used {@link PermutationIndex}.
       * @return The used {@link PermutationIndex}.
       */
      public PermutationIndex<String, IndexData> getPermutationIndex() {
         return permutationIndex;
      }

      /**
       * Returns the total amount of NO_TOOL count.
       * @return The total amount of NO_TOOL count.
       */
      public int getNoToolCountTotal() {
         return noToolCountTotal;
      }

      /**
       * Returns the total amount of SED count.
       * @return The total amount of SED count.
       */
      public int getSedCountTotal() {
         return sedCountTotal;
      }
   }
}
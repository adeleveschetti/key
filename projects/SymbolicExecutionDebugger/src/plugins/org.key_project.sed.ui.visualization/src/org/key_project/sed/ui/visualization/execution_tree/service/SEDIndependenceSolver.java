package org.key_project.sed.ui.visualization.execution_tree.service;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.Assert;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.graphiti.features.impl.IIndependenceSolver;
import org.eclipse.graphiti.ui.features.DefaultFeatureProvider;
import org.key_project.sed.core.model.ISEDDebugElement;
import org.key_project.sed.core.model.ISEDDebugTarget;
import org.key_project.sed.core.util.SEDIterator;
import org.key_project.util.java.ObjectUtil;

/**
 * <p>
 * Implementation of {@link IIndependenceSolver} which can be used to link
 * {@link ISEDDebugElement}s with Graphiti diagram elements. This implementation does
 * the mapping via the the ID of an {@link ISEDDebugElement} ({@link ISEDDebugElement#getId()}).
 * All other elements are mapped with their hash code value ({@link Object#hashCode()}).
 * </p>
 * <p>
 * To use this {@link IIndependenceSolver} it is required to change the
 * default instance in the constructor of the used {@link DefaultFeatureProvider}
 * via {@code setIndependenceSolver}.
 * </p>
 * @author Martin Hentschel
 */
public class SEDIndependenceSolver implements IIndependenceSolver {
   /**
    * Maps the hash code ({@link Object#hashCode()}) to his instance.
    */
   private Map<String, Object> objectHashmap = new HashMap<String, Object>();
   
   /**
    * Contains all available {@link IDebugTarget}s.
    */
   private Set<IDebugTarget> targets = new HashSet<IDebugTarget>();

   /**
    * <p>
    * Initializes this solver if possible with the given business objects.
    * </p>
    * <p>
    * This method must be executed before {@link #getKeyForBusinessObject(Object)}
    * is called the first time.
    * </p>
    * @param targets The given business objects
    * @throws DebugException Occurred Exception.
    */
   public void init(List<ISEDDebugTarget> targets) throws DebugException {
      Assert.isTrue(this.objectHashmap.isEmpty());
      Assert.isTrue(this.targets.isEmpty());
      if (targets != null) {
         for (ISEDDebugTarget target : targets) {
            SEDIterator iter = new SEDIterator(target);
            while (iter.hasNext()) {
               ISEDDebugElement next = iter.next();
               getKeyForBusinessObject(next);
            }
         }
      }
   }
   
   /**
    * {@inheritDoc}
    */
   @Override
   public String getKeyForBusinessObject(Object bo) {
      String key;
      if (bo instanceof ISEDDebugElement) {
         key = ((ISEDDebugElement)bo).getId();
      }
      else {
         key = Integer.toString(ObjectUtil.hashCode(bo));
      }
      objectHashmap.put(key, bo);
      if (bo instanceof IDebugElement) {
         IDebugElement element = (IDebugElement)bo;
         targets.add(element.getDebugTarget());
      }
      return key;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public Object getBusinessObjectForKey(String key) {
      return key != null ? objectHashmap.get(key) : null;
   }
   
   /**
    * Returns all known business objects.
    * @return The known business objects.
    */
   public Collection<Object> getAllBusinessObjects() {
      return objectHashmap.values();
   }

   /**
    * Returns all available {@link IDebugTarget}s.
    * @return The available {@link IDebugTarget}s.
    */
   public IDebugTarget[] getDebugTargets() {
      return targets.toArray(new IDebugTarget[targets.size()]);
   }
}
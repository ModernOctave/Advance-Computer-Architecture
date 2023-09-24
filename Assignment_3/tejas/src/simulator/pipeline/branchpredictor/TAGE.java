/*****************************************************************************

				Tejas Simulator

------------------------------------------------------------------------------------------------------------



   Copyright [2010] [Indian Institute of Technology, Delhi]

   Licensed under the Apache License, Version 2.0 (the "License");

   you may not use this file except in compliance with the License.

   You may obtain a copy of the License at



       http://www.apache.org/licenses/LICENSE-2.0



   Unless required by applicable law or agreed to in writing, software

   distributed under the License is distributed on an "AS IS" BASIS,

   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.

   See the License for the specific language governing permissions and

   limitations under the License.

------------------------------------------------------------------------------------------------------------



	Contributors:  Rikita Ahuja

*****************************************************************************/





package pipeline.branchpredictor;



import pipeline.ExecutionEngine;



/**

 *

 * @author Rikita

 */

public class TAGE extends BranchPredictor {



         /*Predict History Table of size based on the input*/

        int[] PHT;



         /*

         * saturating_states contains the maximum value a saturating counter can have, starting from 0

         * PHTsize is the number of entries the PHT array can have i.e. 2^PCBits

         * maskbits are used to extract the desired bits from the PC address

         * not_taken_states contains the maximum value of a saturating state that gives the prediction as NOT_TAKEN

         */

        int PCBits,saturating_states,maskbits,not_taken_states;

        

        

        public static int predictions = 0;

        public static int aging = 256000;

        

        public static int no_of_tables = 8;

        public static int alpha = 2;

        public static int pred_bits = 3;

        public static int u_bits = 2;

        public static int table_altPred = -1;

        public static int table_Pred = -1;

        public static boolean defPred = false;

        public static boolean alt_Pred = false;

        public static boolean pred = false;

        

        public int[] L;

        

        class TT

        {

        	int[] u;

        	int[] pred;

        	long[] tag;

        	TT(int len)

        	{

        		u = new int[len];

        		pred = new int[len];

        		tag = new long[len];

        		for(int i =0 ; i < len ; i++)

        		{

        			u[i] = 0;

        			pred[i] = 0;

        			tag[i] = -1;

        		}

        	}

        }

        

        public TT[] T;



         /*

         * Constructor Bimodal_Predictor() to take the value of PCBits and number of saturating_bits

         * Also it initialises values of all the member variable of the class

         */ 

        public TAGE(ExecutionEngine containingExecEngine,int PCBits,int saturating_bits)

        {

        		super(containingExecEngine);

        		

                this.PCBits=PCBits;

                this.saturating_states=(1<<saturating_bits)-1;

                maskbits=(1<<PCBits)-1;

                PHT=new int[maskbits+1];

                for(int i=0;i<=maskbits;i++)

                        PHT[i]=saturating_states;

                not_taken_states=(int)(saturating_states/2);

                

                L = new int[no_of_tables];

                T = new TT[no_of_tables];

                L[0] = 0;

                for(int i = 0;  i < no_of_tables ; i++)

                {

                	if( i == 1)

                	{

                		L[i] = 2;

                	}

                	else

                	{
				int pow = 1;
				for(int j = 0; j < i-1 ; j++)
				{
					pow *= alpha;
				} 

                		L[i] = (int)(pow * L[1] + 0.5);

                	}

                	

                	T[i] = new TT(L[i]);

                }

        }



        /*

         * Method <code>Train()</code> used to train the BHR and the corresponding PHT

         * according to the last few branches Taken/Not Taken

         */

        /**

         *

         * @param address takes in the values the PC address whose branch has to be trained

         * @param outcome takes in the actual value of branch taken/not taken

         * @param predict takes in the value which is predicted for the corresponding address

         * <code>true</code> when branch taken otherwise <code>false</code>

         */

        public void Train(long address, boolean outcome, boolean predict) {
        		address &= 0x0000FFFFFFFFFFFFL;
                int index=(int)(address&maskbits);

                int state=PHT[index];

		int in = -1 ;

		if(table_Pred != -1)
                	in = (int)(address % L [table_Pred]) ;

               //update the useful counter 
		
               if(table_Pred != -1 && alt_Pred != pred )
               {

            	   if(pred == outcome)

            	   {

            		   if(T[table_Pred].u[in] != ( (1<< u_bits) - 1) )

            		   {

            			   T[table_Pred].u[in]++;

            		   }

            	   }

            	   else

            	   {

            		   if(T[table_Pred].u[in] != 0 )

            		   {

            			   T[table_Pred].u[in]--;

            		   }

            	   }

               }

               

               //graceful aging

               if(predictions >= aging)

               {

            	   predictions = 0;

            	   for(int i = 1 ; i < no_of_tables; i++)

            	   {

            		   for(int j  = 0; j < L[i]; j++)

            		   {

            			   T[i].u[j] = 0;

            		   }

            	   }

               }

               

               

               //update on correct pred

               if(predict == outcome)

               {

            	   if(table_Pred != -1)

            	   {

            		   if(predict && T[table_Pred].pred[in] != ( (1<< pred_bits) - 1))

            		   {

            			   T[table_Pred].pred[in]++;

            		   }
			   else if(!predict && T[table_Pred].pred[in] != 0)

            		   {

            			   T[table_Pred].pred[in]--;

            		   }

            	   }

            	   else //prediction was from the bimodal predictor

            	   {

            		   if(outcome && state!=saturating_states)

                           state++;

            		   else if(!outcome && state!=0)

                           state--;

            		   PHT[index]=state;

            	   }

               }

               

               

             //update on incorrect pred

               else

               {

            	   if(table_Pred != -1)

            	   {

            		   if(!predict && T[table_Pred].pred[in] != ( (1<< pred_bits) - 1))

            		   {

            			   T[table_Pred].pred[in]++;

            		   }
			   else if(predict && T[table_Pred].pred[in] != 0)

            		   {

            			   T[table_Pred].pred[in]--;

            		   }

            	   }

            	   else //prediction was from the bimodal predictor

            	   {

            		   if(!predict && state!=saturating_states)

                           state++;

            		   else if(predict && state!=0)

                           state--;

            		   PHT[index]=state;

            	   }

            	   

            	   if(table_Pred == -1)

            		   table_Pred = 0;

            	   

            	   int i;

            	   for(i = table_Pred + 1 ; i < no_of_tables ; i++ )

            	   {

            		   in = (int)(address % L [i]);

            		   if(T[i].u[in] == 0)

            		   {

            			   T[i].pred[in] = ( (1<< pred_bits) - 1) / 2 + 1;

            			   T[i].tag[in] = address ;

            			   break;

            		   }

            	   }

            	   if(i== no_of_tables)

            	   {

            		   for(int j = table_Pred + 1 ; j < no_of_tables; j++)

                	   {

            			   in = (int)(address % L [j]);

            			   T[j].u[in]--;

                	   }

            	   }

               }

               



        }



        /*

         * predict the branch taken or not according to the current value of member variable

         * boolean true for branch Taken

         * boolean false for Not Taken

         */

        /**

         *

         * @param address takes in the values the PC address whose branch has to be trained

         * @return <code>true</code> when prediction is branch taken otherwise <code>false</code>

         */

        public boolean predict(long address, boolean outcome) {
    			address &= 0x0000FFFFFFFFFFFFL;

        		predictions++;

                int index=(int)(address&maskbits);

                int state=PHT[index];

                defPred = alt_Pred = pred = false;

                table_altPred = table_Pred = -1;

                if(state > not_taken_states)

                        defPred = true;

                for(int i = no_of_tables-1 ; i > 0 ; i--)

                {

                	int in = (int)(address % L[i]);

                	if(T[i].tag[in] == address)

                	{

                		if(table_Pred == -1)

                		{

                			table_Pred = i;

                			if(T[i].pred[in] > ( ( (1<<pred_bits) - 1) /2 ) )

                				pred = true;

                		}

                		else if(table_altPred == -1)

                		{

                			table_altPred = i;

                			if(T[i].pred[in] > ( ( (1<<pred_bits) - 1) /2 ) )

                				alt_Pred = true;

                		}

                	}

                	if(table_altPred != -1 && table_Pred != -1)

                	{

                		break;

                	}

                }

                if(table_altPred == -1)

                	alt_Pred = defPred;

                if(table_Pred == -1)

                	return alt_Pred;

                else

                	return pred;

        }

}

DROP PROCEDURE IF EXISTS sp_vl_LDL_to_Zero;
$$
CREATE PROCEDURE `sp_vl_LDL_to_Zero`()
BEGIN
    DECLARE done INT DEFAULT 0;
    DECLARE var_obs_id INT;
    DECLARE var_order_id INT;
    DECLARE record_count INT DEFAULT 0;
    DECLARE error_code INT;
    DECLARE error_message VARCHAR(255);
    
    -- loop counter
    DECLARE total_records INT DEFAULT 0;
    DECLARE updated_counter INT DEFAULT 0;
    
    -- temp variables
    DECLARE temp_encounter_id INT;
    DECLARE temp_date_stopped DATETIME;
	DECLARE temp_voided_by INT;
    DECLARE temp_quantitative_order_id INT;
  
	-- Declare cursor for your query
    DECLARE cur CURSOR FOR
        SELECT obs_id, order_id FROM obs where concept_id=1305 and value_coded=1302 and order_id is not null order by obs_id desc;
    
    -- Error Handler
    DECLARE CONTINUE HANDLER FOR SQLEXCEPTION
    BEGIN
	GET DIAGNOSTICS CONDITION 1 error_code = MYSQL_ERRNO, error_message = MESSAGE_TEXT;
        -- Undo previous actions
        ROLLBACK; -- Rollback any changes made before the error occurred
        SELECT CONCAT('An error occurred: ', error_code, ' - ', error_message) as Script_Error;
    END;
    
    -- total records
    SELECT COUNT(*) FROM obs where concept_id=1305 and value_coded=1302 and order_id is not null into total_records;
    SELECT CONCAT("Total Records: ", total_records) as kickoff_status;

    -- Open the cursor
    OPEN cur;
    
    -- Records
    set record_count = 0;
        
    IF total_records > 0 then
		-- Loop through the results
		read_loop: LOOP

			-- If no more rows, exit loop
			IF updated_counter = total_records THEN
				SELECT "Finished Processing Loop" as loop_status;
				LEAVE read_loop;
			END IF;
			
			-- Fetch data into variables
			FETCH cur INTO var_obs_id, var_order_id;
            
            -- Start the transaction
			START TRANSACTION;
            
            SELECT CONCAT("Now Processing Record: ID: ", var_obs_id, " count ", record_count, " of ", total_records) as record_status;
            
            -- clear the temp vars
            set temp_encounter_id = 0;
			set temp_date_stopped = null;
			set temp_voided_by = 0;
			set temp_quantitative_order_id = 0;

			-- Step 1: (obs table) Change the concept_id to 856 and value of result (value_coded = null and value_numeric = 0)
            update obs set concept_id = 856, value_numeric = 0, value_coded = null where obs_id = var_obs_id;
            
            -- Step 2: (orders table) Find corresponding quantitative order
            select encounter_id, date_stopped, voided_by from orders where order_id = var_order_id into temp_encounter_id, temp_date_stopped, temp_voided_by;
            select order_id from orders where encounter_id = temp_encounter_id and concept_id = 856 into temp_quantitative_order_id;
            
            -- Step 3: (orders table) Change end date to null, and voided = true for quanlitative order
            update orders set date_stopped = null, voided = 1, voided_by = temp_voided_by where order_id = var_order_id;
            
            -- Step 4: (orders table) Change end date to above, and voided = false for quantitative order
            update orders set date_stopped = temp_date_stopped, voided = 0, voided_by = null where encounter_id = temp_encounter_id and concept_id = 856;
            
            -- Step 5: (obs table) Change the order_id to the quantitative order
            update obs set order_id = temp_quantitative_order_id where obs_id = var_obs_id;
            
            -- Step 6: (orders table) update the related order
            update orders set concept_id = 856, previous_order_id = temp_quantitative_order_id where previous_order_id = order_id and order_action = "DISCONTINUE";
	
            
			-- clear the temp vars
            set temp_encounter_id = 0;
			set temp_date_stopped = null;
			set temp_voided_by = 0;
			set temp_quantitative_order_id = 0;
            
            -- record count
            set record_count = record_count + 1;
            -- alert
            SELECT CONCAT("Finished Record: ID: ", var_obs_id, " count ", record_count, " of ", total_records) as record_status;
            
			-- commit the transaction or rollback
			ROLLBACK;
			-- COMMIT;
	    set updated_counter = updated_counter + 1;
            
	END LOOP;
	end if;

    -- Close the cursor
    CLOSE cur;

	SELECT CONCAT("Processed Records: ", record_count) as finish_status;
END
$$
